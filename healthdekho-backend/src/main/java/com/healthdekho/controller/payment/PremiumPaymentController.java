package com.healthdekho.controller.payment;

import com.healthdekho.dto.payment.*;
import com.healthdekho.service.payment.PremiumPaymentService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
public class PremiumPaymentController {

    private final PremiumPaymentService paymentService;

    public PremiumPaymentController(PremiumPaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // Process one or multiple installment payments.
    @PostMapping
    public ResponseEntity<List<PremiumPaymentResponse>> processPayments(
            @Valid @RequestBody PremiumPaymentRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {

        List<PremiumPaymentResponse> response =
                paymentService.processPayments(
                        request,
                        userDetails.getUsername()
                );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Get policy payment summary.
    @GetMapping("/policies/{policyNumber}/summary")
    public ResponseEntity<PolicyPaymentSummaryResponse> getPolicySummary(
            @PathVariable String policyNumber,
            @AuthenticationPrincipal UserDetails userDetails
    ) {

        return ResponseEntity.ok(
                paymentService.getPolicyPaymentSummary(
                        policyNumber,
                        userDetails.getUsername()
                )
        );
    }

    // Get all installments for a policy.
    @GetMapping("/policies/{policyNumber}/schedule")
    public ResponseEntity<List<InstallmentScheduleResponse>> getPaymentSchedule(
            @PathVariable String policyNumber,
            @AuthenticationPrincipal UserDetails userDetails
    ) {

        return ResponseEntity.ok(
                paymentService.getPaymentSchedule(
                        policyNumber,
                        userDetails.getUsername()
                )
        );
    }

    // Get payment history for one installment.
    @GetMapping("/installments/{installmentId}/history")
    public ResponseEntity<List<PaymentHistoryResponse>> getPaymentHistory(
            @PathVariable Long installmentId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {

        return ResponseEntity.ok(
                paymentService.getPaymentHistory(
                        installmentId,
                        userDetails.getUsername()
                )
        );
    }
}