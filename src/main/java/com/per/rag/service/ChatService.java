package com.per.rag.service;

import com.per.rag.dto.response.ChatResponse;

import reactor.core.publisher.Flux;

public interface ChatService {

    /**
     * Generate chat response using RAG (non-streaming)
     *
     * @param question User question
     * @return Chat response with answer and sources
     */
    ChatResponse chat(String question);

    /**
     * Generate chat response using RAG (streaming)
     *
     * @param question User question
     * @return Stream of response chunks
     */
    Flux<String> chatStream(String question);
}
