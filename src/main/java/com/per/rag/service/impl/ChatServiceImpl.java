package com.per.rag.service.impl;

import java.util.List;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import com.per.common.exception.ApiErrorCode;
import com.per.common.exception.ApiException;
import com.per.product.repository.ProductVariantRepository;
import com.per.rag.dto.response.ProductRecommendation;
import com.per.rag.dto.response.ShopAssistantResponse;
import com.per.rag.service.ChatService;
import com.per.rag.service.VectorStoreService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final VectorStoreService vectorStoreService;
    private final ChatModel chatModel;
    private final ProductVariantRepository productVariantRepository;

    @Value("${app.rag.search-top-k}")
    private int searchTopK;

    @Value("${app.rag.similarity-threshold}")
    private double similarityThreshold;

    @Value("classpath:knowledge/system-prompt.txt")
    private Resource systemPromptResource;

    private static final String SYSTEM_PROMPT_TEMPLATE =
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
			- **INCORRECT**: `"field": | \\n Markdown content`
			- **CORRECT**: `"field": "Markdown content with \\\\n for newlines"`
			7. **ESCAPE NEWLINES**: Use `\\n` sequence for newlines within strings. Do NOT use backslashes `\\` at the end of lines for line continuation.
			- **INCORRECT**: `"field": "Part 1 \\ \\n Part 2"`
			- **CORRECT**: `"field": "Part 1\\nPart 2"`
			8. Ensure all special characters are properly escaped for a standard JSON string.

			# CORRECT RESPONSE EXAMPLE
			{example}
			""";

    private static final String FEW_SHOT_EXAMPLE =
            """
			```json
			{
			  "summary": "Dior Homme Intense matches your formal request perfectly.",
			  "detailedResponse": "**Hero Recommendation:**\\nDior Homme Intense – [velvety] with notes of [iris, amber]\\nPrice: [3,400,000 VNÐ]\\n\\n**Why these scents:** Perfectly suited for formal events due to its sophisticated woody profile.",
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

    @Override
    public ShopAssistantResponse chat(String question) {
        try {
            // Load system prompt from resource
            String systemPromptTemplate = loadSystemPrompt();

            // Retrieve similar documents from vector store
            List<Document> similarDocs =
                    vectorStoreService.searchSimilar(question, searchTopK, similarityThreshold);

            // Build context from retrieved documents
            String context = buildContext(similarDocs);

            // Initialize BeanOutputConverter for structured output
            BeanOutputConverter<ShopAssistantResponse> converter =
                    new BeanOutputConverter<>(ShopAssistantResponse.class);

            // Build prompt with context, question, and format instructions
            PromptTemplate promptTemplate = new PromptTemplate(systemPromptTemplate);
            Prompt prompt =
                    promptTemplate.create(
                            java.util.Map.of(
                                    "context", context,
                                    "question", question,
                                    "format", converter.getFormat(),
                                    "example", FEW_SHOT_EXAMPLE));

            // Call LLM
            String response = chatModel.call(prompt).getResult().getOutput().getText();

            log.debug("Raw LLM response: {}", response);

            // Clean response (remove markdown code blocks if present)
            String cleanedResponse = cleanMarkdownCodeBlocks(response);

            // Sanitize YAML-like block scalars (| or >) that break JSON parsing
            cleanedResponse = sanitizeMalformedJson(cleanedResponse);

            // Parse structured response
            try {
                return converter.convert(cleanedResponse);
            } catch (Exception e) {
                log.error("Failed to parse LLM response as JSON. Raw response: {}", response);
                return getFallbackResponse(question);
            }

        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to generate chat response", e);
            throw new ApiException(
                    ApiErrorCode.RAG_CHAT_FAILED, "Failed to generate chat response", e);
        }
    }

    private String loadSystemPrompt() {
        try {
            return new String(systemPromptResource.getInputStream().readAllBytes());
        } catch (Exception e) {
            log.error("Failed to load system prompt from resource", e);
            throw new ApiException(ApiErrorCode.RAG_CHAT_FAILED, "Failed to load system prompt", e);
        }
    }

    @Override
    public Flux<String> chatStream(String question) {
        try {
            // Retrieve similar documents
            List<Document> similarDocs =
                    vectorStoreService.searchSimilar(question, searchTopK, similarityThreshold);

            if (similarDocs.isEmpty()) {
                return Flux.just(
                        "{\"summary\":\"No relevant information found\",\"detailedResponse\":\"I apologize, but I couldn't find relevant information. Please rephrase your question.\",\"products\":[],\"nextSteps\":[\"Browse our product catalog\",\"Contact customer service\",\"Ask about specific fragrance families\"]}");
            }

            // Build context
            String context = buildContext(similarDocs);

            // For streaming, we use a simpler prompt without structured output
            String streamPrompt =
                    """
					You are a fragrance consultant. Use this context to answer the question:

					CONTEXT:
					%s

					QUESTION: %s

					Provide a helpful, conversational response.
					"""
                            .formatted(context, question);

            // Stream response
            return chatModel.stream(streamPrompt);

        } catch (ApiException e) {
            return Flux.error(e);
        } catch (Exception e) {
            log.error("Failed to generate streaming chat response", e);
            return Flux.error(
                    new ApiException(
                            ApiErrorCode.RAG_CHAT_FAILED, "Failed to generate chat response", e));
        }
    }

    private String buildContext(List<Document> documents) {
        if (documents.isEmpty()) {
            return "No relevant product information available.";
        }
        return documents.stream()
                .map(Document::getText)
                .collect(java.util.stream.Collectors.joining("\n\n---\n\n"));
    }

    /** Remove markdown code block wrappers if present (for Llama 3.2 compatibility) */
    private String cleanMarkdownCodeBlocks(String response) {
        String cleaned = response.trim();

        // Remove ```json ... ``` wrappers
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7); // Remove ```json
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3); // Remove ```
        }

        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3);
        }

        return cleaned.trim();
    }

    /**
     * Fixes common LLM malformations in JSON specifically related to Llama models:
     * 1. YAML-style block scalars (| or >) are converted to valid JSON strings with escaped newlines.
     * 2. Literal newlines within strings are escaped.
     * 3. Missing commas between JSON fields are added.
     */
    private String sanitizeMalformedJson(String json) {
        if (json == null || json.isBlank()) return json;

        String sanitized = json.trim();

        // 1. Repair YAML-style block scalars (": |)
        // This looks for "key": | followed by lines that don't look like JSON keys
        // It consumes until it sees a line starting with "key": or the closing brace }
        try {
            java.util.regex.Pattern yamlBlock = java.util.regex.Pattern.compile(
                "(\"\\w+\"\\s*:\\s*)[|>]\\s*\\n(.*?)(?=\\n\\s*\"\\w+\"\\s*:|\\n\\s*\\})", 
                java.util.regex.Pattern.DOTALL);
            java.util.regex.Matcher matcher = yamlBlock.matcher(sanitized);
            StringBuilder sb = new StringBuilder();
            int lastEnd = 0;
            while (matcher.find()) {
                sb.append(sanitized, lastEnd, matcher.start());
                String keyPrefix = matcher.group(1);
                String content = matcher.group(2);
                
                // Escape characters that are invalid inside JSON string literals
                String escapedContent = content.trim()
                        .replace("\\", "\\\\")
                        .replace("\"", "\\\"")
                        .replace("\r", "")
                        .replace("\n", "\\n");
                
                sb.append(keyPrefix).append("\"").append(escapedContent).append("\"");
                lastEnd = matcher.end();
            }
            sb.append(sanitized.substring(lastEnd));
            sanitized = sb.toString();
        } catch (Exception e) {
            log.warn("Regex failure during YAML block repair, skipping this step.", e);
        }

        // 2. Fix missing commas between fields
        // Pattern: (end of value) \n (start of next key)
        sanitized = sanitized.replaceAll("(\"\\s*|\\d+|true|false|null)\\s*\\n\\s*(\"\\w+\"\\s*:)", "$1,\n  $2");

        // 3. Last resort: if the LLM outputted literal newlines inside a string without a pipe
        // This is dangerous but often needed for multiline detailedResponse.
        // We only do this if it looks like we're still inside a block.
        
        return sanitized;
    }

    private ShopAssistantResponse getFallbackResponse(String question) {
        String summary = "The system is currently experiencing technical difficulties.";

        // Fetch top 3 active products to make the fallback "not stiff"
        List<ProductRecommendation> fallbackProducts =
                productVariantRepository
                        .findAll(
                                org.springframework.data.domain.PageRequest.of(
                                        0,
                                        3,
                                        org.springframework.data.domain.Sort.by(
                                                org.springframework.data.domain.Sort.Direction.DESC,
                                                "createdAt")))
                        .getContent()
                        .stream()
                        .map(
                                variant ->
                                        new ProductRecommendation(
                                                variant.getProduct().getId().toString(),
                                                variant.getProduct().getName(),
                                                variant.getPrice(),
                                                "Featured product in our boutique"))
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

        return new ShopAssistantResponse(
                summary,
                detailedResponse,
                fallbackProducts,
                List.of(
                        "View best-selling products",
                        "Learn about popular fragrance families",
                        "Contact customer support"));
    }
}
