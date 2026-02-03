package com.per.rag.service.impl;

import java.util.Map;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
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
import com.per.rag.service.MultiCollectionSearchService;
import com.per.rag.service.MultiCollectionSearchService.MultiSearchResult;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

/**
 * Implementation of ChatService for AI-powered fragrance consultation. Uses multi-collection
 * retrieval across brands, categories, products, and knowledge.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final MultiCollectionSearchService multiSearchService;
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
            // Load system prompt
            String systemPromptTemplate = promptHelper.loadSystemPrompt();

            // Multi-source retrieval across all collections
            MultiSearchResult searchResult =
                    multiSearchService.searchAll(question, searchTopK, similarityThreshold);

            // Build multi-source context with labeled sections
            String context = contextBuilderHelper.buildMultiSourceContext(searchResult);

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

            // Clean response
            String cleanedResponse = jsonSanitizerHelper.cleanMarkdownCodeBlocks(response);
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
            // Multi-source retrieval
            MultiSearchResult searchResult =
                    multiSearchService.searchAll(question, searchTopK, similarityThreshold);

            if (searchResult.products().isEmpty() && searchResult.knowledge().isEmpty()) {
                return Flux.just(
                        "{\"summary\":\"No relevant information found\",\"detailedResponse\":\"I apologize, but I couldn't find relevant information. Please rephrase your question.\",\"products\":[],\"nextSteps\":[\"Browse our product catalog\",\"Contact customer service\",\"Ask about specific fragrance families\"]}");
            }

            // Build multi-source context
            String context = contextBuilderHelper.buildMultiSourceContext(searchResult);

            // Streaming prompt
            String streamPrompt =
                    """
					You are a fragrance consultant. Use this context to answer the question:

					%s

					QUESTION: %s

					Provide a helpful, conversational response.
					"""
                            .formatted(context, question);

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
