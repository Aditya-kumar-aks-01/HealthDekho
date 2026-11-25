package com.healthdekho.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "insurance_product")
public class InsuranceProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long productId;

    @Column(name = "product_code", nullable = false, unique = true)
    private String productCode;

    @Column(name = "policy_name", nullable = false)
    private String policyName;

    @Column(name = "policy_type", nullable = false)
    private String policyType;

    @Column(name = "term_months", nullable = false)
    private Integer termMonths;

    @Column(name = "premium_frequency", nullable = false)
    private String premiumFrequency;

    @Column(name = "premium_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal premiumAmount;

    @Column(name = "sum_insured", nullable = false, precision = 19, scale = 2)
    private BigDecimal sumInsured;

    @Column(name = "min_entry_age", nullable = false)
    private Integer minEntryAge;

    @Column(name = "max_entry_age", nullable = false)
    private Integer maxEntryAge;

    @Column(nullable = false)
    private Boolean active = true;

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    private List<CustomerPolicy> customerPolicies = new ArrayList<>();

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    private List<AiRecommendationItem> recommendationItems = new ArrayList<>();

    protected InsuranceProduct() {
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getPolicyName() {
        return policyName;
    }

    public void setPolicyName(String policyName) {
        this.policyName = policyName;
    }

    public String getPolicyType() {
        return policyType;
    }

    public void setPolicyType(String policyType) {
        this.policyType = policyType;
    }

    public Integer getTermMonths() {
        return termMonths;
    }

    public void setTermMonths(Integer termMonths) {
        this.termMonths = termMonths;
    }

    public String getPremiumFrequency() {
        return premiumFrequency;
    }

    public void setPremiumFrequency(String premiumFrequency) {
        this.premiumFrequency = premiumFrequency;
    }

    public BigDecimal getPremiumAmount() {
        return premiumAmount;
    }

    public void setPremiumAmount(BigDecimal premiumAmount) {
        this.premiumAmount = premiumAmount;
    }

    public BigDecimal getSumInsured() {
        return sumInsured;
    }

    public void setSumInsured(BigDecimal sumInsured) {
        this.sumInsured = sumInsured;
    }

    public Integer getMinEntryAge() {
        return minEntryAge;
    }

    public void setMinEntryAge(Integer minEntryAge) {
        this.minEntryAge = minEntryAge;
    }

    public Integer getMaxEntryAge() {
        return maxEntryAge;
    }

    public void setMaxEntryAge(Integer maxEntryAge) {
        this.maxEntryAge = maxEntryAge;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public List<CustomerPolicy> getCustomerPolicies() {
        return customerPolicies;
    }

    public void setCustomerPolicies(List<CustomerPolicy> customerPolicies) {
        this.customerPolicies = customerPolicies;
    }

    public List<AiRecommendationItem> getRecommendationItems() {
        return recommendationItems;
    }

    public void setRecommendationItems(List<AiRecommendationItem> recommendationItems) {
        this.recommendationItems = recommendationItems;
    }
}