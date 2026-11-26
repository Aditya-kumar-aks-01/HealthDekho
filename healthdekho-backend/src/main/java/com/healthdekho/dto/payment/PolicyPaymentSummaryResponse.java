package com.healthdekho.dto.payment;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PolicyPaymentSummaryResponse(

        Long policyId,
        String policyNumber,
        String customerName,
        String policyName,
        Integer termMonths,
        LocalDate startDate,
        LocalDate maturityDate,
        BigDecimal totalPremiumPayable,
        BigDecimal totalPremiumPaid,
        BigDecimal totalPenalty,
        BigDecimal remainingPremiumAmount,
        String policyStatus

) {}