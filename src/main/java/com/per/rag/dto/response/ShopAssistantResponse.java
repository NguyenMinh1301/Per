package com.per.rag.dto.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShopAssistantResponse {

    @JsonProperty(required = true)
    @JsonPropertyDescription("A short, direct answer in 1-2 sentences")
    private String summary;

    @JsonProperty(required = true)
    @JsonPropertyDescription("The full response with detailed explanations in Markdown format")
    private String detailedResponse;

    @JsonProperty(required = true)
    @JsonPropertyDescription("List of recommended products mentioned in the response")
    private List<ProductRecommendation> products;

    @JsonProperty(required = true)
    @JsonPropertyDescription("3 suggested follow-up questions or actions for the user")
    private List<String> nextSteps;
}
