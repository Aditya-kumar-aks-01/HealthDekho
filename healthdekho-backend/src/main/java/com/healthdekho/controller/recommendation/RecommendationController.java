package com.healthdekho.controller.recommendation;

import com.healthdekho.dto.recommendation.RecommendationRequest;
import com.healthdekho.dto.recommendation.RecommendationResponse;
import com.healthdekho.service.recommendation.RecommendationService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(
            RecommendationService recommendationService
    ) {
        this.recommendationService = recommendationService;
    }

    @PostMapping
    public ResponseEntity<RecommendationResponse> generateRecommendations(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody RecommendationRequest request
    ) {

        RecommendationResponse response =
                recommendationService.generateRecommendations(
                        userDetails.getUsername(),
                        request
                );

        return ResponseEntity.ok(response);
    }
}