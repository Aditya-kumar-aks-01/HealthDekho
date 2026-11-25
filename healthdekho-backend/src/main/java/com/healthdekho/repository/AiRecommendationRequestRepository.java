package com.healthdekho.repository;

import com.healthdekho.entity.AiRecommendationRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AiRecommendationRequestRepository
        extends JpaRepository<AiRecommendationRequest, Long> {

    List<AiRecommendationRequest>
    findByCustomer_CustomerIdOrderByRequestedAtDesc(Long customerId);
}