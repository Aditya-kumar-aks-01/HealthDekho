
package com.healthdekho.service.recommendation;

import com.healthdekho.dto.recommendation.AiRecommendationOutput;
import com.healthdekho.dto.recommendation.AiRecommendationOutputItem;
import com.healthdekho.dto.recommendation.RecommendationRequest;
import com.healthdekho.dto.recommendation.RecommendationResponse;
import com.healthdekho.dto.recommendation.RecommendedProductResponse;

import com.healthdekho.entity.AiRecommendationItem;
import com.healthdekho.entity.AiRecommendationRequest;
import com.healthdekho.entity.Customer;
import com.healthdekho.entity.CustomerPolicy;
import com.healthdekho.entity.InsuranceProduct;

import com.healthdekho.exception.AiRecommendationException;
import com.healthdekho.exception.CustomerNotFoundException;

import com.healthdekho.repository.AiRecommendationItemRepository;
import com.healthdekho.repository.AiRecommendationRequestRepository;
import com.healthdekho.repository.CustomerPolicyRepository;
import com.healthdekho.repository.CustomerRepository;
import com.healthdekho.repository.InsuranceProductRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.Period;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    private static final int MAX_RECOMMENDATIONS = 5;
    private static final String PROMPT_VERSION = "v1";

    private final CustomerRepository customerRepository;
    private final CustomerPolicyRepository customerPolicyRepository;
    private final InsuranceProductRepository insuranceProductRepository;
    private final AiRecommendationRequestRepository recommendationRequestRepository;
    private final AiRecommendationItemRepository recommendationItemRepository;
    private final BedrockRecommendationClient bedrockRecommendationClient;

    public RecommendationService(
            CustomerRepository customerRepository,
            CustomerPolicyRepository customerPolicyRepository,
            InsuranceProductRepository insuranceProductRepository,
            AiRecommendationRequestRepository recommendationRequestRepository,
            AiRecommendationItemRepository recommendationItemRepository,
            BedrockRecommendationClient bedrockRecommendationClient
    ) {
        this.customerRepository = customerRepository;
        this.customerPolicyRepository = customerPolicyRepository;
        this.insuranceProductRepository = insuranceProductRepository;
        this.recommendationRequestRepository = recommendationRequestRepository;
        this.recommendationItemRepository = recommendationItemRepository;
        this.bedrockRecommendationClient = bedrockRecommendationClient;
    }

    @Transactional
    public RecommendationResponse generateRecommendations(
            String customerEmail,
            RecommendationRequest request
    ) {

        Customer customer = customerRepository.findByEmail(customerEmail)
                .orElseThrow(() ->
                        new CustomerNotFoundException("Customer not found.")
                );

        LocalDate dateOfBirth = customer.getDateOfBirth();

        if (dateOfBirth == null || dateOfBirth.isAfter(LocalDate.now())) {
            throw new AiRecommendationException(
                    "A valid date of birth is required in your customer profile."
            );
        }

        int age = Period.between(dateOfBirth, LocalDate.now()).getYears();

        List<CustomerPolicy> existingPolicies =
                customerPolicyRepository.findByCustomer_CustomerId(
                        customer.getCustomerId()
                );

        Set<Long> ownedProductIds = existingPolicies.stream()
                .map(CustomerPolicy::getProduct)
                .filter(product -> product != null)
                .map(InsuranceProduct::getProductId)
                .collect(Collectors.toSet());

        List<InsuranceProduct> eligibleProducts =
                insuranceProductRepository.findByActiveTrue()
                        .stream()
                        .filter(product ->
                                !ownedProductIds.contains(product.getProductId())
                        )
                        .filter(product ->
                                age >= product.getMinEntryAge()
                                        && age <= product.getMaxEntryAge()
                        )
                        .toList();

        AiRecommendationOutput aiOutput =
                bedrockRecommendationClient.generateRecommendations(
                        age,
                        request.insuranceGoals(),
                        request.preferredTerm(),
                        request.preferredPolicyType(),
                        eligibleProducts
                );

        OffsetDateTime requestedAt = OffsetDateTime.now();

        AiRecommendationRequest recommendationRequest =
                new AiRecommendationRequest();

        recommendationRequest.setCustomer(customer);
        recommendationRequest.setInsuranceGoals(request.insuranceGoals());
        recommendationRequest.setPreferredTerm(request.preferredTerm());
        recommendationRequest.setPreferredPolicyType(
                request.preferredPolicyType()
        );
        recommendationRequest.setCalculatedAge(age);
        recommendationRequest.setModelName(
                bedrockRecommendationClient.getModelId()
        );
        recommendationRequest.setModelVersion("Amazon Bedrock Converse");
        recommendationRequest.setPromptVersion(PROMPT_VERSION);
        recommendationRequest.setRequestedAt(requestedAt);

        recommendationRequest =
                recommendationRequestRepository.save(recommendationRequest);

        Map<Long, InsuranceProduct> eligibleProductMap =
                eligibleProducts.stream()
                        .collect(Collectors.toMap(
                                InsuranceProduct::getProductId,
                                Function.identity()
                        ));

        Set<Long> alreadyRecommended = new HashSet<>();

        List<AiRecommendationItem> itemsToSave = new ArrayList<>();

        int rank = 1;

        for (AiRecommendationOutputItem aiItem :
                aiOutput.recommendations()) {

            if (rank > MAX_RECOMMENDATIONS) {
                break;
            }

            if (aiItem == null || aiItem.productId() == null) {
                continue;
            }

            InsuranceProduct product =
                    eligibleProductMap.get(aiItem.productId());

            // Never trust a product ID supplied by the AI.
            if (product == null || !alreadyRecommended.add(product.getProductId())) {
                continue;
            }

            AiRecommendationItem item = new AiRecommendationItem();

            item.setRecommendationRequest(recommendationRequest);
            item.setProduct(product);
            item.setRankOrder(rank++);
            item.setSuitability(normalizeSuitability(aiItem.suitability()));
            item.setRecommendationReason(
                    sanitizeReason(aiItem.recommendationReason())
            );
            item.setSelectedForRegistration(false);

            itemsToSave.add(item);
        }

        List<AiRecommendationItem> savedItems =
                recommendationItemRepository.saveAll(itemsToSave);

        List<RecommendedProductResponse> recommendations =
                savedItems.stream()
                        .map(this::toResponse)
                        .toList();

        return new RecommendationResponse(
                recommendationRequest.getRecommendationRequestId(),
                age,
                requestedAt,
                recommendations
        );
    }

    private String normalizeSuitability(String suitability) {

        if (suitability == null) {
            return "MEDIUM";
        }

        return switch (suitability.trim().toUpperCase()) {
            case "HIGH", "MEDIUM", "LOW" -> suitability.trim().toUpperCase();
            default -> "MEDIUM";
        };
    }

    private String sanitizeReason(String reason) {

        if (reason == null || reason.isBlank()) {
            return "This product matches some of your stated insurance preferences.";
        }

        return reason.length() > 1000
                ? reason.substring(0, 1000)
                : reason;
    }

    private RecommendedProductResponse toResponse(AiRecommendationItem item) {

        InsuranceProduct product = item.getProduct();

        return new RecommendedProductResponse(
                item.getRecommendationItemId(),
                item.getRankOrder(),
                product.getProductId(),
                product.getProductCode(),
                product.getPolicyName(),
                product.getPolicyType(),
                product.getTermMonths(),
                product.getSumInsured(),
                product.getPremiumFrequency(),
                product.getPremiumAmount(),
                item.getSuitability(),
                item.getRecommendationReason()
        );
    }
}
