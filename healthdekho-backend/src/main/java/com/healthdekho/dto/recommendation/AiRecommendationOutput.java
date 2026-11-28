package com.healthdekho.dto.recommendation;

import java.util.List;

public record AiRecommendationOutput(
        List<AiRecommendationOutputItem> recommendations
) {
}