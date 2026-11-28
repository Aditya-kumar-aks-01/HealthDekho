package com.healthdekho.dto.recommendation;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record RecommendationResponse(
        Long recommendationRequestId,
        Integer calculatedAge,
        OffsetDateTime requestedAt,
        List<RecommendedProductResponse> recommendations
) {
}