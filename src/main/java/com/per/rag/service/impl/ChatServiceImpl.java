package com.per.rag.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.ai.document.Document;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.per.common.exception.ApiErrorCode;
import com.per.common.exception.ApiException;
import com.per.rag.dto.response.ChatResponse;
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
    private final OllamaChatModel chatModel;

    @Value("${app.rag.search-top-k}")
    private int searchTopK;

    @Value("${app.rag.similarity-threshold}")
    private double similarityThreshold;

    @Override
    public ChatResponse chat(String question) {
        try {
            // Retrieve similar documents
            List<Document> similarDocs =
                    vectorStoreService.searchSimilar(question, searchTopK, similarityThreshold);

            if (similarDocs.isEmpty()) {
                return ChatResponse.builder()
                        .answer(
                                "I apologize, but I couldn't find relevant information about that. Could you please rephrase your question or ask about our available perfume products?")
                        .build();
            }

            // Build context from documents
            String context = buildContext(similarDocs);

            // Generate response directly from context
            String answer = chatModel.call(context);

            return ChatResponse.builder().answer(answer).build();

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
                        "I apologize, but I couldn't find relevant information about that. Could you please rephrase your question or ask about our available perfume products?");
            }

            // Build context and generate response
            String context = buildContext(similarDocs);

            // Stream response directly from context
            return chatModel.stream(context);

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
        return documents.stream().map(Document::getText).collect(Collectors.joining("\n\n---\n\n"));
    }
}
