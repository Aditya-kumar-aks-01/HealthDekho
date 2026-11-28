package com.healthdekho.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ai_recommendation_request")
public class AiRecommendationRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recommendation_request_id")
    private Long recommendationRequestId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

   @Column(name = "insurance_goals", columnDefinition = "TEXT")
private String insuranceGoals;

    @Column(name = "preferred_term")
    private String preferredTerm;

    @Column(name = "preferred_policy_type")
    private String preferredPolicyType;

    @Column(name = "calculated_age")
    private Integer calculatedAge;

    @Column(name = "model_name", nullable = false)
    private String modelName;

    @Column(name = "model_version")
    private String modelVersion;

    @Column(name = "prompt_version")
    private String promptVersion;

    @Column(name = "requested_at", nullable = false)
    private OffsetDateTime requestedAt;

    @OneToMany(mappedBy = "recommendationRequest", fetch = FetchType.LAZY)
    private List<AiRecommendationItem> recommendationItems = new ArrayList<>();

    public AiRecommendationRequest() {
    }

    public Long getRecommendationRequestId() {
        return recommendationRequestId;
    }

    public void setRecommendationRequestId(Long recommendationRequestId) {
        this.recommendationRequestId = recommendationRequestId;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public String getInsuranceGoals() {
        return insuranceGoals;
    }

    public void setInsuranceGoals(String insuranceGoals) {
        this.insuranceGoals = insuranceGoals;
    }

    public String getPreferredTerm() {
        return preferredTerm;
    }

    public void setPreferredTerm(String preferredTerm) {
        this.preferredTerm = preferredTerm;
    }

    public String getPreferredPolicyType() {
        return preferredPolicyType;
    }

    public void setPreferredPolicyType(String preferredPolicyType) {
        this.preferredPolicyType = preferredPolicyType;
    }

    public Integer getCalculatedAge() {
        return calculatedAge;
    }

    public void setCalculatedAge(Integer calculatedAge) {
        this.calculatedAge = calculatedAge;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getModelVersion() {
        return modelVersion;
    }

    public void setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
    }

    public String getPromptVersion() {
        return promptVersion;
    }

    public void setPromptVersion(String promptVersion) {
        this.promptVersion = promptVersion;
    }

    public OffsetDateTime getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(OffsetDateTime requestedAt) {
        this.requestedAt = requestedAt;
    }

    public List<AiRecommendationItem> getRecommendationItems() {
        return recommendationItems;
    }

    public void setRecommendationItems(List<AiRecommendationItem> recommendationItems) {
        this.recommendationItems = recommendationItems;
    }
}