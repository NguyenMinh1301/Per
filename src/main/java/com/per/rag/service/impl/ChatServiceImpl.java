package com.per.rag.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.per.common.exception.ApiErrorCode;
import com.per.common.exception.ApiException;
import com.per.rag.dto.response.ShopAssistantResponse;
import com.per.rag.handler.FallbackResponseHandler;
import com.per.rag.helper.ContextBuilderHelper;
import com.per.rag.helper.JsonSanitizerHelper;
import com.per.rag.helper.PromptHelper;
import com.per.rag.service.ChatService;
import com.per.rag.service.VectorStoreService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

/**
 * Implementation of ChatService for AI-powered fragrance consultation. Orchestrates RAG (Retrieval
 * Augmented Generation) workflow by delegating to helper and handler classes.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final VectorStoreService vectorStoreService;
    private final ChatModel chatModel;
    private final PromptHelper promptHelper;
    private final JsonSanitizerHelper jsonSanitizerHelper;
    private final ContextBuilderHelper contextBuilderHelper;
    private final FallbackResponseHandler fallbackResponseHandler;

    @Value("${app.rag.search-top-k}")
    private int searchTopK;

    @Value("${app.rag.similarity-threshold}")
    private double similarityThreshold;

    @Override
    public ShopAssistantResponse chat(String question) {
        try {
            // Load system prompt from resource
            String systemPromptTemplate = promptHelper.loadSystemPrompt();

            // Retrieve similar documents from vector store
            List<Document> similarDocs =
                    vectorStoreService.searchSimilar(question, searchTopK, similarityThreshold);

            // Build context from retrieved documents
            String context = contextBuilderHelper.buildContext(similarDocs);

            // Initialize BeanOutputConverter for structured output
            BeanOutputConverter<ShopAssistantResponse> converter =
                    new BeanOutputConverter<>(ShopAssistantResponse.class);

            // Build prompt with context, question, and format instructions
            PromptTemplate promptTemplate = new PromptTemplate(systemPromptTemplate);
            Prompt prompt =
                    promptTemplate.create(
                            Map.of(
                                    "context",
                                    context,
                                    "question",
                                    question,
                                    "format",
                                    converter.getFormat(),
                                    "example",
                                    PromptHelper.FEW_SHOT_EXAMPLE));

            // Call LLM
            String response = chatModel.call(prompt).getResult().getOutput().getText();

            log.debug("Raw LLM response: {}", response);

            // Clean response (remove markdown code blocks if present)
            String cleanedResponse = jsonSanitizerHelper.cleanMarkdownCodeBlocks(response);

            // Sanitize YAML-like block scalars (| or >) that break JSON parsing
            cleanedResponse = jsonSanitizerHelper.sanitizeMalformedJson(cleanedResponse);

            // Parse structured response
            try {
                return converter.convert(cleanedResponse);
            } catch (Exception e) {
                log.error("Failed to parse LLM response as JSON. Raw response: {}", response);
                return fallbackResponseHandler.getFallbackResponse(question);
            }

        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to generate chat response", e);
            throw new ApiException(
                    ApiErrorCode.RAG_CHAT_FAILED, "Failed to generate chat response", e);
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
            String context = contextBuilderHelper.buildContext(similarDocs);

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
}
