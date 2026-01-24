package com.per.rag.helper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import com.per.common.exception.ApiErrorCode;
import com.per.common.exception.ApiException;

import lombok.extern.slf4j.Slf4j;

/**
 * Helper class for loading and managing AI prompt templates. Centralizes all prompt-related
 * constants and loading logic.
 */
@Slf4j
@Component
public class PromptHelper {

    @Value("classpath:knowledge/system-prompt.txt")
    private Resource systemPromptResource;

    /**
     * Minimal prompt template treating LLM as a pure data conversion pipeline (XML → JSON). Removes
     * conversational tone to prevent hallucination.
     */
    public static final String SYSTEM_PROMPT_TEMPLATE =
            """
			# ROLE
			You are a RAG Conversion Engine.
			INPUT: Product data in XML format and a User Query.
			OUTPUT: Valid JSON object only.

			# DATA CONTEXT
			{context}

			# USER QUERY
			{question}

			# OUTPUT SCHEMA
			{format}

			# STRICT RULES
			1. IGNORE all conversational fillers. Do NOT say "Here is the JSON" or "Step 1...".
			2. ACT as a pure data pipeline.
			3. DATA SOURCE: Use ONLY the content inside <inventory> tags.
			4. MAPPING:
			- Extract 'id' from <product_item><id>
			- Extract 'name' from <metadata><productName>
			- Extract 'price' from <content> (parse "Price Range:" line, use minimum value)
			- Generate 'summary' as 1-2 sentence direct answer
			- Generate 'detailedResponse' in Markdown format with product recommendations
			- Generate 'reasonForRecommendation' explaining why product matches query
			- Generate 'nextSteps' as 3 follow-up suggestions
			5. If <inventory> is empty or contains <empty> tag, return empty 'products' list with polite apology in 'summary'.
			6. Maximum 3 products in 'products' array.
			7. Return ONLY raw JSON - no markdown code blocks, no conversational text.
			8. Use \\n for newlines in JSON strings. Escape all special characters properly.

			# CORRECT RESPONSE EXAMPLE
			{example}
			""";

    /**
     * Minimal few-shot example showing only the JSON structure. No conversational wrapper to
     * prevent hallucination.
     */
    public static final String FEW_SHOT_EXAMPLE =
            """
			{
			"summary": "Found matching products for your query.",
			"detailedResponse": "**Recommended:**\\nProduct Name – descriptive attributes\\nPrice: X VND\\n\\n**Why:** Matches your criteria.",
			"products": [
				{
				"id": "EXTRACT_FROM_<id>_TAG",
				"name": "EXTRACT_FROM_<productName>",
				"price": 0.0,
				"reasonForRecommendation": "Matches criteria"
				}
			],
			"nextSteps": ["Check availability", "View similar products", "Contact support"]
			}
			""";

    /**
     * Loads the system prompt from the classpath resource.
     *
     * @return The system prompt template as a String
     * @throws ApiException if the resource cannot be loaded
     */
    public String loadSystemPrompt() {
        try {
            return new String(systemPromptResource.getInputStream().readAllBytes());
        } catch (Exception e) {
            log.error("Failed to load system prompt from resource", e);
            throw new ApiException(ApiErrorCode.RAG_CHAT_FAILED, "Failed to load system prompt", e);
        }
    }
}
