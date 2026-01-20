package com.per.common.init.rag;

import org.springframework.context.ApplicationEvent;

/**
 * Event published when Ollama initialization completes. Used to trigger RAG indexing after models
 * are available.
 */
public class OllamaReadyEvent extends ApplicationEvent {

    private final boolean success;

    public OllamaReadyEvent(Object source, boolean success) {
        super(source);
        this.success = success;
    }

    public boolean isSuccess() {
        return success;
    }
}
