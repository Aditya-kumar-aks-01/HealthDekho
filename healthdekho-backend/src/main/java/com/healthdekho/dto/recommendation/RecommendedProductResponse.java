package com.healthdekho.dto.recommendation;

import java.math.BigDecimal;

public record RecommendedProductResponse(
        Long recommendationItemId,
        Integer rankOrder,
        Long productId,
        String productCode,
        String policyName,
        String policyType,
        Integer termMonths,
        BigDecimal sumInsured,
        String premiumFrequency,
        BigDecimal premiumAmount,
        String suitability,
        String recommendationReason
) {
}