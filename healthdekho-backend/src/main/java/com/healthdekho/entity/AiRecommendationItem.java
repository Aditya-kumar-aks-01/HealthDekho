package com.healthdekho.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "ai_recommendation_item")
public class AiRecommendationItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recommendation_item_id")
    private Long recommendationItemId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recommendation_request_id", nullable = false)
    private AiRecommendationRequest recommendationRequest;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private InsuranceProduct product;

    @Column(name = "rank_order", nullable = false)
    private Integer rankOrder;

    @Column(name = "recommendation_reason", columnDefinition = "TEXT")
private String recommendationReason;

    private String suitability;

    @Column(name = "selected_for_registration", nullable = false)
    private Boolean selectedForRegistration = false;

    protected AiRecommendationItem() {
    }

    public Long getRecommendationItemId() {
        return recommendationItemId;
    }

    public void setRecommendationItemId(Long recommendationItemId) {
        this.recommendationItemId = recommendationItemId;
    }

    public AiRecommendationRequest getRecommendationRequest() {
        return recommendationRequest;
    }

    public void setRecommendationRequest(AiRecommendationRequest recommendationRequest) {
        this.recommendationRequest = recommendationRequest;
    }

    public InsuranceProduct getProduct() {
        return product;
    }

    public void setProduct(InsuranceProduct product) {
        this.product = product;
    }

    public Integer getRankOrder() {
        return rankOrder;
    }

    public void setRankOrder(Integer rankOrder) {
        this.rankOrder = rankOrder;
    }

    public String getRecommendationReason() {
        return recommendationReason;
    }

    public void setRecommendationReason(String recommendationReason) {
        this.recommendationReason = recommendationReason;
    }

    public String getSuitability() {
        return suitability;
    }

    public void setSuitability(String suitability) {
        this.suitability = suitability;
    }

    public Boolean getSelectedForRegistration() {
        return selectedForRegistration;
    }

    public void setSelectedForRegistration(Boolean selectedForRegistration) {
        this.selectedForRegistration = selectedForRegistration;
    }
}

