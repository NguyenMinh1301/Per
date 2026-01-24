package com.per.rag.handler;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.per.product.repository.ProductVariantRepository;
import com.per.rag.dto.response.ProductRecommendation;
import com.per.rag.dto.response.ShopAssistantResponse;

import lombok.RequiredArgsConstructor;

/**
 * Handler for generating fallback responses when the main chat flow fails. Provides graceful
 * degradation with helpful product suggestions.
 */
@Component
@RequiredArgsConstructor
public class FallbackResponseHandler {

    private final ProductVariantRepository productVariantRepository;

    /**
     * Generates a fallback response when the AI chat fails or returns malformed JSON. Fetches the
     * latest 3 products to provide helpful suggestions.
     *
     * @param question The original user question (currently unused but kept for future
     *     enhancements)
     * @return A fallback ShopAssistantResponse with error message and product suggestions
     */
    public ShopAssistantResponse getFallbackResponse(String question) {
        String summary = "The system is currently experiencing technical difficulties.";

        // Fetch top 3 active products to make the fallback "not stiff"
        List<ProductRecommendation> fallbackProducts =
                productVariantRepository
                        .findAll(PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "createdAt")))
                        .getContent()
                        .stream()
                        .map(
                                variant ->
                                        ProductRecommendation.builder()
                                                .id(variant.getProduct().getId().toString())
                                                .name(variant.getProduct().getName())
                                                .price(variant.getPrice())
                                                .reasonForRecommendation(
                                                        "Featured product in our boutique")
                                                .build())
                        .toList();

        String detailedResponse =
                """
				### System Notification

				I apologize, but the system is currently having trouble processing your detailed request. This might be due to high traffic or a temporary technical issue.

				However, based on your interest, I've suggested some of our most popular fragrances currently loved at our boutique below.

				**Suggestions for you:**
				* Explore the latest collections in our store.
				* Search by fragrance family (Floral, Woody, Marine, etc.).
				* Contact a consultant for personalized assistance.

				Thank you for your patience and for choosing Per!
				""";

        return ShopAssistantResponse.builder()
                .summary(summary)
                .detailedResponse(detailedResponse)
                .products(fallbackProducts)
                .nextSteps(
                        List.of(
                                "View best-selling products",
                                "Learn about popular fragrance families",
                                "Contact customer support"))
                .build();
    }
}
