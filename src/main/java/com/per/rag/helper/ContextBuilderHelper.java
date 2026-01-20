package com.per.rag.helper;

import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;

/**
 * Helper class for building context strings from document lists. Formats retrieved documents for
 * inclusion in LLM prompts.
 */
@Component
public class ContextBuilderHelper {

    /**
     * Builds a formatted context string from a list of documents. Joins document texts with
     * separator markers for clarity.
     *
     * @param documents List of documents retrieved from vector store
     * @return Formatted context string for LLM prompt
     */
    public String buildContext(List<Document> documents) {
        if (documents.isEmpty()) {
            return "No relevant product information available.";
        }
        return documents.stream()
                .map(Document::getText)
                .collect(java.util.stream.Collectors.joining("\n\n---\n\n"));
    }
}
