
package com.healthdekho.dto.product;

import java.math.BigDecimal;

public record InsuranceProductResponse(
        Long productId,
        String productCode,
        String policyName,
        String policyType,
        Integer termMonths,
        String premiumFrequency,
        BigDecimal premiumAmount,
        BigDecimal sumInsured,
        Integer minEntryAge,
        Integer maxEntryAge,
        Boolean active
) {}
