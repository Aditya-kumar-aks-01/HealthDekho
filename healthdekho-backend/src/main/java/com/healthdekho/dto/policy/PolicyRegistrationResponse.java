
package com.healthdekho.dto.policy;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PolicyRegistrationResponse(
        Long policyId,
        String policyNumber,
        String policyName,
        String policyType,
        Integer termMonths,
        LocalDate startDate,
        LocalDate maturityDate,
        String premiumFrequency,
        BigDecimal premiumAmount,
        BigDecimal sumInsured,
        BigDecimal totalPremiumPayable,
        String status,
        int totalInstallments,
        String message
) {}
