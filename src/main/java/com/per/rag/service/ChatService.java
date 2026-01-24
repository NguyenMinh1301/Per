package com.per.rag.service;

import com.per.rag.dto.response.ShopAssistantResponse;

import reactor.core.publisher.Flux;

public interface ChatService {

    /** Generate a structured chat response with product recommendations */
    ShopAssistantResponse chat(String question);

    /** Stream chat response (simple text streaming) */
    Flux<String> chatStream(String question);
}
