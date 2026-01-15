package com.per.rag.dto.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record ShopAssistantResponse(
        @JsonProperty(required = true)
                @JsonPropertyDescription("A short, direct answer in 1-2 sentences")
                String summary,
        @JsonProperty(required = true)
                @JsonPropertyDescription(
                        "The full response with detailed explanations in Markdown format")
                String detailedResponse,
        @JsonProperty(required = true)
                @JsonPropertyDescription("List of recommended products mentioned in the response")
                List<ProductRecommendation> products,
        @JsonProperty(required = true)
                @JsonPropertyDescription("3 suggested follow-up questions or actions for the user")
                List<String> nextSteps) {}
