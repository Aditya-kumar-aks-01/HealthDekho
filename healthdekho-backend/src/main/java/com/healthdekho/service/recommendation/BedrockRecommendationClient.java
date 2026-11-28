
package com.healthdekho.service.recommendation;

import com.healthdekho.dto.recommendation.AiRecommendationOutput;
import com.healthdekho.entity.InsuranceProduct;
import com.healthdekho.exception.AiRecommendationException;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class BedrockRecommendationClient {

    private final ChatClient chatClient;
    private final String modelId;

    public BedrockRecommendationClient(
            ChatClient.Builder chatClientBuilder,
            @Value("${spring.ai.bedrock.converse.chat.options.model}")
            String modelId
    ) {
        this.chatClient = chatClientBuilder.build();
        this.modelId = modelId;
    }

    public String getModelId() {
        return modelId;
    }

    public AiRecommendationOutput generateRecommendations(
            Integer customerAge,
            String insuranceGoals,
            String preferredTerm,
            String preferredPolicyType,
            List<InsuranceProduct> eligibleProducts
    ) {

        if (eligibleProducts == null || eligibleProducts.isEmpty()) {
            return new AiRecommendationOutput(List.of());
        }

        String productCatalogue = eligibleProducts.stream()
                .map(this::formatProduct)
                .collect(Collectors.joining("\n"));

        String systemPrompt = """
                You are an insurance recommendation assistant for HealthDekho.

                Your task is to recommend suitable insurance products from the
                supplied eligible product catalogue.

                STRICT RULES:
                1. Recommend products ONLY from the supplied catalogue.
                2. Use the exact productId provided in the catalogue.
                3. Never invent product IDs, product names, premiums, or benefits.
                4. Do not calculate or modify premium amounts or sum insured.
                5. Consider customer age, insurance goals, preferred term,
                   and preferred policy type.
                6. Return at most 5 recommendations.
                7. Rank the most suitable products first.
                8. Give a concise, specific reason for each recommendation.
                9. Suitability must be one of: HIGH, MEDIUM, LOW.
                10. If no product is suitable, return an empty recommendations list.

                Return the result in the requested structured format.
                """;

        String userPrompt = """
                Recommend suitable insurance products for this customer.

                CUSTOMER DETAILS:
                Age: %d
                Insurance goals: %s
                Preferred term: %s
                Preferred policy type: %s

                ELIGIBLE PRODUCT CATALOGUE:
                %s

                Return only recommendations for products from this catalogue.
                """.formatted(
                customerAge,
                insuranceGoals,
                preferredTerm,
                preferredPolicyType == null || preferredPolicyType.isBlank()
                        ? "No specific preference"
                        : preferredPolicyType,
                productCatalogue
        );

        try {
            AiRecommendationOutput output = chatClient.prompt()
                    .system(systemPrompt)
                    .user(userPrompt)
                    .call()
                    .entity(AiRecommendationOutput.class);

            if (output == null || output.recommendations() == null) {
                throw new AiRecommendationException(
                        "The AI service returned an invalid recommendation response."
                );
            }

            return output;

        } catch (AiRecommendationException exception) {
            throw exception;

        } catch (Exception exception) {
            throw new AiRecommendationException(
                    "Unable to generate insurance recommendations at this time.",
                    exception
            );
        }
    }

    private String formatProduct(InsuranceProduct product) {

        return """
                {
                  "productId": %d,
                  "productCode": "%s",
                  "policyName": "%s",
                  "policyType": "%s",
                  "termMonths": %d,
                  "premiumFrequency": "%s",
                  "premiumAmount": "%s",
                  "sumInsured": "%s",
                  "minEntryAge": %d,
                  "maxEntryAge": %d
                }
                """.formatted(
                product.getProductId(),
                safe(product.getProductCode()),
                safe(product.getPolicyName()),
                safe(product.getPolicyType()),
                product.getTermMonths(),
                safe(product.getPremiumFrequency()),
                product.getPremiumAmount(),
                product.getSumInsured(),
                product.getMinEntryAge(),
                product.getMaxEntryAge()
        );
    }

    private String safe(String value) {
        return Objects.requireNonNullElse(value, "")
                .replace("\"", "\\\"");
    }
}
