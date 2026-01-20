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
     * Hardcoded system prompt template with placeholders for context, question, format, and
     * example. Used as fallback or when resource loading fails.
     */
    public static final String SYSTEM_PROMPT_TEMPLATE =
            """
			You are a sophisticated, friendly fragrance consultant at a high-end boutique.
			Your goal is to help customers find their signature scent and encourage purchases.

			CONTEXT (Product Information and Knowledge Base):
			{context}

			USER QUESTION:
			{question}

			CRITICAL INSTRUCTIONS:
			1. FILTERING: Only select products from CONTEXT that strictly match user intent.
			- Summer request → IGNORE heavy/woody scents like Santal 33
			- Office request → IGNORE loud gourmands/heavy orientals
			- Match seasonality, occasion, fragrance family appropriately

			2. TONE: Warm, concise, evocative. Use sensory language (crisp, breezy, sun-drenched, velvety, magnetic).

			3. RESPONSE STRUCTURE:
			- summary: 1-2 sentences direct answer
			- detailedResponse: Full explanation in Markdown with:
				 * Hero product recommendation (brand + 2 sensory adjectives + 2-3 key notes)
				 * 1-2 alternatives (same structure)
				 * Prices mentioned naturally (e.g., "starting at 2,500,000 VNĐ")
				 * End with a Call to Action (check availability, add to cart, try sample)
			- products: Extract mentioned products with id, name, price, reasonForRecommendation
			- nextSteps: 3 suggested follow-up questions

			4. LANGUAGE: Match the language of USER QUESTION (Vietnamese/English).

			5. CONSTRAINT: Only mention products in CONTEXT. If none suitable, apologize and ask clarifying questions.

			6. **STRICT JSON ENCODING**: Do NOT use multiline block scalars like `|` or `>`.
			- **INCORRECT**: `"field": | \\\\n Markdown content`
			- **CORRECT**: `"field": "Markdown content with \\\\\\\\n for newlines"`
			7. **ESCAPE NEWLINES**: Use `\\\\n` sequence for newlines within strings. Do NOT use backslashes `\\\\` at the end of lines for line continuation.
			- **INCORRECT**: `"field": "Part 1 \\\\ \\\\n Part 2"`
			- **CORRECT**: `"field": "Part 1\\\\nPart 2"`
			8. Ensure all special characters are properly escaped for a standard JSON string.

			# CORRECT RESPONSE EXAMPLE
			{example}
			""";

    /** Few-shot example showing correct JSON formatting for the AI response. */
    public static final String FEW_SHOT_EXAMPLE =
            """
			```json
			{
			"summary": "Dior Homme Intense matches your formal request perfectly.",
			"detailedResponse": "**Hero Recommendation:**\\\\nDior Homme Intense – [velvety] with notes of [iris, amber]\\\\nPrice: [3,400,000 VNĐ]\\\\n\\\\n**Why these scents:** Perfectly suited for formal events due to its sophisticated woody profile.",
			"products": [
				{
				"id": "Dior_H_Intense_ID",
				"name": "Dior Homme Intense",
				"price": 3400000.0,
				"reasonForRecommendation": "Sophisticated woody profile for formal events"
				}
			],
			"nextSteps": [
				"Check availability",
				"Explore similar woody scents",
				"Learn about fragrance layering"
			]
			}
			```
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
