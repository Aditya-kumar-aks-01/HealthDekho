package com.healthdekho.repository;

import com.healthdekho.entity.AiRecommendationItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AiRecommendationItemRepository
        extends JpaRepository<AiRecommendationItem, Long> {

    List<AiRecommendationItem>
    findByRecommendationRequest_RecommendationRequestIdOrderByRankOrderAsc(
            Long recommendationRequestId);
}   