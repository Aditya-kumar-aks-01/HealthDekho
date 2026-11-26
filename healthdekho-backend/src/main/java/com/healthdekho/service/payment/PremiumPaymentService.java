package com.healthdekho.service.payment;

import com.healthdekho.dto.payment.*;
import com.healthdekho.entity.CustomerPolicy;
import com.healthdekho.entity.PremiumInstallment;
import com.healthdekho.entity.PremiumPayment;
import com.healthdekho.exception.PaymentNotFoundException;
import com.healthdekho.exception.PaymentProcessingException;
import com.healthdekho.repository.CustomerPolicyRepository;
import com.healthdekho.repository.PremiumInstallmentRepository;
import com.healthdekho.repository.PremiumPaymentRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class PremiumPaymentService {

    private static final BigDecimal LATE_PENALTY_RATE =
            new BigDecimal("0.10");

    private static final BigDecimal ZERO =
            new BigDecimal("0.00");

    private final PremiumInstallmentRepository installmentRepository;
    private final PremiumPaymentRepository paymentRepository;
    private final CustomerPolicyRepository policyRepository;

    public PremiumPaymentService(
            PremiumInstallmentRepository installmentRepository,
            PremiumPaymentRepository paymentRepository,
            CustomerPolicyRepository policyRepository
    ) {
        this.installmentRepository = installmentRepository;
        this.paymentRepository = paymentRepository;
        this.policyRepository = policyRepository;
    }

    // ---------------------------------------------------------
    // 1. PROCESS ONE OR MULTIPLE PAYMENTS
    // ---------------------------------------------------------

    @Transactional
    public List<PremiumPaymentResponse> processPayments(
            PremiumPaymentRequest request,
            String customerEmail
    ) {

        // Prevent duplicate installment IDs in one request.
        Set<Long> installmentIds = new HashSet<>();

        for (InstallmentPaymentRequest item : request.payments()) {
            if (!installmentIds.add(item.installmentId())) {
                throw new PaymentProcessingException(
                        "Duplicate installment found in the payment request."
                );
            }
        }

        // Sort IDs to acquire database locks in a consistent order.
        List<InstallmentPaymentRequest> sortedPayments =
                request.payments().stream()
                        .sorted((a, b) ->
                                a.installmentId().compareTo(b.installmentId()))
                        .toList();

        List<PremiumPaymentResponse> responses = new ArrayList<>();

        for (InstallmentPaymentRequest item : sortedPayments) {

            PremiumInstallment installment =
                    installmentRepository
                            .findByIdAndCustomerEmail(
                                    item.installmentId(),
                                    customerEmail
                            )
                            .orElseThrow(() ->
                                    new PaymentNotFoundException(
                                            "Installment not found for this customer."
                                    )
                            );

            responses.add(processSinglePayment(installment, item));
        }

        return responses;
    }

    // ---------------------------------------------------------
    // 2. PROCESS A SINGLE INSTALLMENT PAYMENT
    // ---------------------------------------------------------

    private PremiumPaymentResponse processSinglePayment(
            PremiumInstallment installment,
            InstallmentPaymentRequest request
    ) {

        LocalDate today = LocalDate.now();

        BigDecimal scheduledAmount = installment.getScheduledAmount();

        BigDecimal alreadyPaid = installment.getPaidAmount();

        BigDecimal totalPenalty =
                paymentRepository.calculateTotalPenalty(
                        installment.getInstallmentId()
                );

        if (totalPenalty == null) {
            totalPenalty = ZERO;
        }

        // A paid installment cannot receive another payment.
        BigDecimal currentOutstanding = scheduledAmount
                .add(totalPenalty)
                .subtract(alreadyPaid);

        if (currentOutstanding.compareTo(ZERO) <= 0) {
            throw new PaymentProcessingException(
                    "This installment has already been fully paid."
            );
        }

        BigDecimal requestedAmount = request.amount()
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal newPenalty = ZERO;

        // Apply penalty only once, when the first late payment occurs.
        if (today.isAfter(installment.getGracePeriodEndDate())
                && totalPenalty.compareTo(ZERO) == 0) {

            BigDecimal unpaidPremium = scheduledAmount.subtract(alreadyPaid);

            if (unpaidPremium.compareTo(ZERO) > 0) {
                newPenalty = unpaidPremium
                        .multiply(LATE_PENALTY_RATE)
                        .setScale(2, RoundingMode.HALF_UP);
            }
        }

        BigDecimal updatedTotalPenalty = totalPenalty.add(newPenalty);

        BigDecimal outstandingBeforePayment = scheduledAmount
                .add(updatedTotalPenalty)
                .subtract(alreadyPaid);

        if (requestedAmount.compareTo(outstandingBeforePayment) > 0) {
            throw new PaymentProcessingException(
                    "Payment exceeds the outstanding amount. Outstanding amount: "
                            + outstandingBeforePayment
            );
        }

        // Generate a unique transaction reference.
        String transactionReference =
                "TXN-HD-" + UUID.randomUUID()
                        .toString()
                        .replace("-", "")
                        .substring(0, 16)
                        .toUpperCase();

        OffsetDateTime paymentDate = OffsetDateTime.now();

        // Save immutable payment transaction.
        PremiumPayment payment = new PremiumPayment();

        payment.setInstallment(installment);
        payment.setTransactionReference(transactionReference);
        payment.setPaymentDate(paymentDate);
        payment.setAmountPaid(requestedAmount);
        payment.setPenaltyAmount(newPenalty);
        payment.setPaymentMethod(
                request.paymentMethod().trim().toUpperCase()
        );
        payment.setCreatedAt(paymentDate);

        paymentRepository.save(payment);

        // Update cumulative money collected.
        BigDecimal updatedPaidAmount = alreadyPaid.add(requestedAmount);

        installment.setPaidAmount(updatedPaidAmount);

        // Determine installment payment status.
        BigDecimal finalAmountDue = scheduledAmount
                .add(updatedTotalPenalty);

        String paymentStatus = determinePaymentStatus(
                updatedPaidAmount,
                finalAmountDue,
                installment.getGracePeriodEndDate(),
                today
        );

        installment.setPaymentStatus(paymentStatus);

        installmentRepository.save(installment);

        // Update the parent policy status if all installments are paid.
        updatePolicyStatus(installment.getPolicy());

        BigDecimal outstandingAfterPayment = finalAmountDue
                .subtract(updatedPaidAmount);

        return new PremiumPaymentResponse(
                payment.getPaymentId(),
                transactionReference,
                installment.getInstallmentId(),
                installment.getInstallmentNumber(),
                requestedAmount,
                newPenalty,
                outstandingAfterPayment,
                paymentStatus,
                paymentDate,
                "Payment processed successfully."
        );
    }

    // ---------------------------------------------------------
    // 3. PAYMENT STATUS USING SWITCH EXPRESSION
    // ---------------------------------------------------------

    private String determinePaymentStatus(
            BigDecimal paidAmount,
            BigDecimal totalDue,
            LocalDate gracePeriodEndDate,
            LocalDate today
    ) {

        boolean fullyPaid = paidAmount.compareTo(totalDue) >= 0;
        boolean partiallyPaid = paidAmount.compareTo(ZERO) > 0;
        boolean overdue = today.isAfter(gracePeriodEndDate);

        String state = fullyPaid ? "FULLY_PAID"
                : partiallyPaid ? "PARTIALLY_PAID"
                : overdue ? "OVERDUE"
                : "PENDING";

        return switch (state) {
            case "FULLY_PAID" -> "PAID";
            case "PARTIALLY_PAID" -> "PARTIALLY_PAID";
            case "OVERDUE" -> "OVERDUE";
            default -> "PENDING";
        };
    }

    // ---------------------------------------------------------
    // 4. UPDATE POLICY STATUS
    // ---------------------------------------------------------

    private void updatePolicyStatus(CustomerPolicy policy) {

        List<PremiumInstallment> installments =
                installmentRepository
                        .findByPolicy_PolicyIdOrderByInstallmentNumberAsc(
                                policy.getPolicyId()
                        );

        BigDecimal totalPenalty = installments.stream()
                .map(i -> paymentRepository.calculateTotalPenalty(
                        i.getInstallmentId()
                ))
                .filter(p -> p != null)
                .reduce(ZERO, BigDecimal::add);

        boolean allPaid = installments.stream().allMatch(installment -> {

            BigDecimal penalty =
                    paymentRepository.calculateTotalPenalty(
                            installment.getInstallmentId()
                    );

            if (penalty == null) {
                penalty = ZERO;
            }

            BigDecimal totalDue = installment.getScheduledAmount()
                    .add(penalty);

            return installment.getPaidAmount()
                    .compareTo(totalDue) >= 0;
        });

        policy.setStatus(allPaid ? "FULLY_PAID" : "ACTIVE");

        policyRepository.save(policy);
    }

    // ---------------------------------------------------------
    // 5. GET POLICY PAYMENT SUMMARY
    // ---------------------------------------------------------

    @Transactional(readOnly = true)
    public PolicyPaymentSummaryResponse getPolicyPaymentSummary(
            String policyNumber,
            String customerEmail
    ) {

        CustomerPolicy policy = policyRepository
                .findByPolicyNumberAndCustomer_Email(
                        policyNumber,
                        customerEmail
                )
                .orElseThrow(() ->
                        new PaymentNotFoundException(
                                "Policy not found for this customer."
                        )
                );

        List<PremiumInstallment> installments =
                installmentRepository
                        .findByPolicy_PolicyIdOrderByInstallmentNumberAsc(
                                policy.getPolicyId()
                        );

        BigDecimal totalPaid = ZERO;
        BigDecimal totalPenalty = ZERO;
        BigDecimal remainingAmount = ZERO;

        for (PremiumInstallment installment : installments) {

            BigDecimal penalty =
                    paymentRepository.calculateTotalPenalty(
                            installment.getInstallmentId()
                    );

            if (penalty == null) {
                penalty = ZERO;
            }

            totalPaid = totalPaid.add(installment.getPaidAmount());
            totalPenalty = totalPenalty.add(penalty);

            remainingAmount = remainingAmount.add(
                    installment.getScheduledAmount()
                            .add(penalty)
                            .subtract(installment.getPaidAmount())
            );
        }

        return new PolicyPaymentSummaryResponse(
                policy.getPolicyId(),
                policy.getPolicyNumber(),
                policy.getCustomer().getFullName(),
                policy.getProduct().getPolicyName(),
                policy.getProduct().getTermMonths(),
                policy.getStartDate(),
                policy.getMaturityDate(),
                policy.getTotalPremiumPayable(),
                totalPaid,
                totalPenalty,
                remainingAmount.max(ZERO),
                policy.getStatus()
        );
    }

    // ---------------------------------------------------------
    // 6. GET INSTALLMENT PAYMENT SCHEDULE
    // ---------------------------------------------------------

    @Transactional(readOnly = true)
    public List<InstallmentScheduleResponse> getPaymentSchedule(
            String policyNumber,
            String customerEmail
    ) {

        CustomerPolicy policy = policyRepository
                .findByPolicyNumberAndCustomer_Email(
                        policyNumber,
                        customerEmail
                )
                .orElseThrow(() ->
                        new PaymentNotFoundException(
                                "Policy not found for this customer."
                        )
                );

        LocalDate today = LocalDate.now();

        return installmentRepository
                .findByPolicy_PolicyIdOrderByInstallmentNumberAsc(
                        policy.getPolicyId()
                )
                .stream()
                .map(installment -> {

                    BigDecimal penalty =
                            paymentRepository.calculateTotalPenalty(
                                    installment.getInstallmentId()
                            );

                    if (penalty == null) {
                        penalty = ZERO;
                    }

                    BigDecimal outstanding = installment
                            .getScheduledAmount()
                            .add(penalty)
                            .subtract(installment.getPaidAmount())
                            .max(ZERO);

                    String status = determinePaymentStatus(
                            installment.getPaidAmount(),
                            installment.getScheduledAmount().add(penalty),
                            installment.getGracePeriodEndDate(),
                            today
                    );

                    return new InstallmentScheduleResponse(
                            installment.getInstallmentId(),
                            installment.getInstallmentNumber(),
                            YearMonth.from(installment.getDueDate()),
                            installment.getScheduledAmount(),
                            installment.getPaidAmount(),
                            penalty,
                            outstanding,
                            installment.getDueDate(),
                            installment.getGracePeriodEndDate(),
                            status
                    );
                })
                .toList();
    }

    // ---------------------------------------------------------
    // 7. GET PAYMENT HISTORY
    // ---------------------------------------------------------

    @Transactional(readOnly = true)
    public List<PaymentHistoryResponse> getPaymentHistory(
            Long installmentId,
            String customerEmail
    ) {

        // This ownership check prevents customers from viewing
        // another customer's payment history.
        installmentRepository
                .findByIdAndCustomerEmail(
                        installmentId,
                        customerEmail
                )
                .orElseThrow(() ->
                        new PaymentNotFoundException(
                                "Installment not found for this customer."
                        )
                );

        return paymentRepository
                .findPaymentHistory(installmentId, customerEmail)
                .stream()
                .map(payment -> new PaymentHistoryResponse(
                        payment.getPaymentId(),
                        payment.getTransactionReference(),
                        payment.getInstallment().getInstallmentId(),
                        payment.getAmountPaid(),
                        payment.getPenaltyAmount(),
                        payment.getPaymentMethod(),
                        payment.getPaymentDate()
                ))
                .toList();
    }
}