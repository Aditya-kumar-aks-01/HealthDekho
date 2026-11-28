
package com.healthdekho.service.policy;

import com.healthdekho.dto.policy.PolicyRegistrationRequest;
import com.healthdekho.dto.policy.PolicyRegistrationResponse;
import com.healthdekho.dto.policy.CustomerPolicyResponse;
import com.healthdekho.entity.Customer;
import com.healthdekho.entity.CustomerPolicy;
import com.healthdekho.entity.InsuranceProduct;
import com.healthdekho.entity.PremiumInstallment;

import com.healthdekho.exception.CustomerNotFoundException;
import com.healthdekho.exception.DuplicatePolicyException;
import com.healthdekho.exception.PolicyRegistrationException;
import com.healthdekho.exception.ProductNotFoundException;

import com.healthdekho.repository.CustomerPolicyRepository;
import com.healthdekho.repository.CustomerRepository;
import com.healthdekho.repository.InsuranceProductRepository;
import com.healthdekho.repository.PremiumInstallmentRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class PolicyService {

    private final CustomerRepository customerRepository;
    private final InsuranceProductRepository productRepository;
    private final CustomerPolicyRepository policyRepository;
    private final PremiumInstallmentRepository installmentRepository;

    public PolicyService(
            CustomerRepository customerRepository,
            InsuranceProductRepository productRepository,
            CustomerPolicyRepository policyRepository,
            PremiumInstallmentRepository installmentRepository
    ) {
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.policyRepository = policyRepository;
        this.installmentRepository = installmentRepository;
    }

    @Transactional
    public PolicyRegistrationResponse registerPolicy(
            String customerEmail,
            PolicyRegistrationRequest request
    ) {

        // 1. Find the authenticated customer.
        Customer customer = customerRepository.findByEmail(customerEmail)
                .orElseThrow(() ->
                        new CustomerNotFoundException("Customer not found.")
                );

        // 2. Find the selected insurance product.
        InsuranceProduct product = productRepository.findById(
                request.productId()
        ).orElseThrow(() ->
                new ProductNotFoundException("Insurance product not found.")
        );

        // 3. Only active products can be registered.
        if (!Boolean.TRUE.equals(product.getActive())) {
            throw new PolicyRegistrationException(
                    "This insurance product is currently inactive."
            );
        }

        // 4. Determine the policy start date.
        LocalDate startDate = request.startDate() != null
                ? request.startDate()
                : LocalDate.now();

        // 5. Validate the customer's date of birth.
        LocalDate dateOfBirth = customer.getDateOfBirth();

        if (dateOfBirth == null || dateOfBirth.isAfter(LocalDate.now())) {
            throw new PolicyRegistrationException(
                    "Customer date of birth is invalid."
            );
        }

        // 6. Validate age eligibility on the policy start date.
        int customerAge = Period.between(
                dateOfBirth,
                startDate
        ).getYears();

        if (customerAge < product.getMinEntryAge()
                || customerAge > product.getMaxEntryAge()) {

            throw new PolicyRegistrationException(
                    "Customer is not eligible for this insurance product. "
                    + "Eligible age range: "
                    + product.getMinEntryAge()
                    + " to "
                    + product.getMaxEntryAge()
                    + " years."
            );
        }

        // 7. Prevent duplicate registration.
        boolean alreadyRegistered =
                policyRepository
                        .existsByCustomer_CustomerIdAndProduct_ProductId(
                                customer.getCustomerId(),
                                product.getProductId()
                        );

        if (alreadyRegistered) {
            throw new DuplicatePolicyException(
                    "You have already registered this insurance product."
            );
        }

        // 8. Validate product term and premium.
        int termMonths = product.getTermMonths();

        if (termMonths <= 0
                || product.getPremiumAmount() == null
                || product.getPremiumAmount().compareTo(BigDecimal.ZERO) <= 0
                || product.getSumInsured() == null
                || product.getSumInsured().compareTo(BigDecimal.ZERO) <= 0) {

            throw new PolicyRegistrationException(
                    "Insurance product contains invalid financial details."
            );
        }
        

        // 9. Determine the installment interval.
        int frequencyMonths = getFrequencyMonths(
                product.getPremiumFrequency()
        );

        if (termMonths % frequencyMonths != 0) {
            throw new PolicyRegistrationException(
                    "Policy term must be divisible by the premium frequency."
            );
        }

        // 10. Calculate installment count and total premium.
        int totalInstallments = termMonths / frequencyMonths;

        BigDecimal totalPremiumPayable =
                product.getPremiumAmount().multiply(
                        BigDecimal.valueOf(totalInstallments)
                );

        // 11. Calculate maturity date.
        LocalDate maturityDate = startDate.plusMonths(termMonths);

        OffsetDateTime now = OffsetDateTime.now();

        // 12. Generate a unique policy number.
        String policyNumber = generatePolicyNumber();

        // 13. Create the policy.
        CustomerPolicy policy = new CustomerPolicy(
                policyNumber,
                customer,
                product,
                startDate,
                maturityDate,
                product.getPremiumFrequency(),
                product.getPremiumAmount(),
                product.getSumInsured(),
                totalPremiumPayable,
                "ACTIVE",
                now
        );

        CustomerPolicy savedPolicy = policyRepository.save(policy);

        // 14. Generate the installment schedule.
        List<PremiumInstallment> installments = new ArrayList<>();

        for (int i = 1; i <= totalInstallments; i++) {

            LocalDate dueDate = startDate.plusMonths(
                    (long) frequencyMonths * i
            );

            LocalDate gracePeriodEndDate = dueDate.plusMonths(1);

            PremiumInstallment installment = new PremiumInstallment(
                    savedPolicy,
                    i,
                    dueDate,
                    gracePeriodEndDate,
                    product.getPremiumAmount(),
                    BigDecimal.ZERO,
                    "PENDING"
            );

            installments.add(installment);
        }

        installmentRepository.saveAll(installments);

        // 15. Return the registration confirmation.
        return new PolicyRegistrationResponse(
                savedPolicy.getPolicyId(),
                savedPolicy.getPolicyNumber(),
                product.getPolicyName(),
                product.getPolicyType(),
                termMonths,
                startDate,
                maturityDate,
                product.getPremiumFrequency(),
                product.getPremiumAmount(),
                product.getSumInsured(),
                totalPremiumPayable,
                savedPolicy.getStatus(),
                totalInstallments,
                "Insurance policy registered successfully."
        );
    }

@Transactional(readOnly = true)
public List<CustomerPolicyResponse> getCustomerPolicies(String customerEmail) {

    // Find the authenticated customer.
    Customer customer = customerRepository.findByEmail(customerEmail)
            .orElseThrow(() ->
                    new CustomerNotFoundException("Customer not found.")
            );

    // Retrieve only this customer's policies.
    List<CustomerPolicy> policies =
            policyRepository.findByCustomer_CustomerId(
                    customer.getCustomerId()
            );

    // Convert policy entities into response DTOs.
    return policies.stream()
            .map(policy -> new CustomerPolicyResponse(
                    policy.getPolicyNumber(),
                    policy.getProduct().getPolicyName(),
                    policy.getProduct().getPolicyType(),
                    policy.getProduct().getTermMonths(),
                    policy.getStartDate(),
                    policy.getMaturityDate(),
                    policy.getPremiumFrequency(),
                    policy.getPremiumAmount(),
                    policy.getSumInsured(),
                    policy.getStatus()
            ))
            .toList();
}


    private int getFrequencyMonths(String frequency) {

        if (frequency == null) {
            throw new PolicyRegistrationException(
                    "Premium frequency is missing."
            );
        }

        String normalizedFrequency = frequency
                .replace("-", "")
                .replace("_", "")
                .replace(" ", "")
                .toUpperCase(Locale.ROOT);

        return switch (normalizedFrequency) {
            case "MONTHLY" -> 1;
            case "QUARTERLY" -> 3;
            case "HALFYEARLY", "SEMIANNUALLY" -> 6;
            case "YEARLY", "ANNUALLY" -> 12;
            default -> throw new PolicyRegistrationException(
                    "Unsupported premium frequency: " + frequency
            );
        };
    }

    private String generatePolicyNumber() {

        String year = String.valueOf(LocalDate.now().getYear());

        String uniquePart = UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 8)
                .toUpperCase(Locale.ROOT);

        return "HDP-" + year + "-" + uniquePart;
    }
}
