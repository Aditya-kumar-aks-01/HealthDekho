package com.healthdekho.dto.recommendation;

public record AiRecommendationOutputItem(
        Long productId,
        String suitability,
        String recommendationReason
) {
}