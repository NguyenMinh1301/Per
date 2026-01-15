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

    @Value("${app.rag.search-top-k}")
    private int searchTopK;

    @Value("${app.rag.similarity-threshold}")
    private double similarityThreshold;

    @Value("classpath:prompt/system-prompt.txt")
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

			{format}

			IMPORTANT: Return ONLY the raw JSON object. Do NOT wrap it in markdown code blocks or add any text before/after the JSON.
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
                                    "format", converter.getFormat()));

            // Call LLM
            String response = chatModel.call(prompt).getResult().getOutput().getText();

            log.debug("Raw LLM response: {}", response);

            // Clean response (remove markdown code blocks if present)
            String cleanedResponse = cleanMarkdownCodeBlocks(response);

            // Parse structured response
            return converter.convert(cleanedResponse);

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
}
