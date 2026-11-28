package com.healthdekho.dto.recommendation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RecommendationRequest(

        @NotBlank(message = "Insurance goals are required")
        @Size(max = 2000, message = "Insurance goals cannot exceed 2000 characters")
        String insuranceGoals,

        @NotBlank(message = "Preferred term is required")
        String preferredTerm,

        String preferredPolicyType
) {
}