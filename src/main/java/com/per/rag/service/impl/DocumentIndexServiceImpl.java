package com.per.rag.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.per.common.exception.ApiErrorCode;
import com.per.common.exception.ApiException;
import com.per.rag.service.DocumentIndexService;
import com.per.rag.service.VectorStoreService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentIndexServiceImpl implements DocumentIndexService {

    private static final String KNOWLEDGE_BASE_PATH = "src/main/resources/knowledge";
    private static final String KNOWLEDGE_TYPE = "knowledge";

    private final VectorStore vectorStore;
    private final VectorStoreService vectorStoreService;

    @Override
    @Transactional
    public void indexKnowledgeBase() {
        try {
            log.info("Starting knowledge base indexing");
            Path knowledgePath = Paths.get(KNOWLEDGE_BASE_PATH);

            if (!Files.exists(knowledgePath)) {
                log.warn("Knowledge base directory does not exist: {}", KNOWLEDGE_BASE_PATH);
                throw new ApiException(
                        ApiErrorCode.RAG_KNOWLEDGE_INDEX_FAILED,
                        "Knowledge base directory not found");
            }

            List<Document> documents = new ArrayList<>();

            try (Stream<Path> paths = Files.walk(knowledgePath)) {
                paths.filter(Files::isRegularFile)
                        .filter(p -> p.toString().endsWith(".md"))
                        .forEach(
                                path -> {
                                    try {
                                        Document doc = createDocumentFromFile(path);
                                        documents.add(doc);
                                    } catch (IOException e) {
                                        log.error("Failed to read file: {}", path, e);
                                    }
                                });
            }

            if (!documents.isEmpty()) {
                vectorStore.add(documents);
                log.info("Indexed {} knowledge base documents successfully", documents.size());
            } else {
                log.warn("No markdown files found in knowledge base");
            }

        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to index knowledge base", e);
            throw new ApiException(
                    ApiErrorCode.RAG_KNOWLEDGE_INDEX_FAILED, "Failed to index knowledge base", e);
        }
    }

    @Override
    @Transactional
    public void indexSingleDocument(String filename) {
        try {
            Path filePath = Paths.get(KNOWLEDGE_BASE_PATH, filename);

            if (!Files.exists(filePath)) {
                throw new ApiException(
                        ApiErrorCode.RAG_KNOWLEDGE_INDEX_FAILED, "File not found: " + filename);
            }

            if (!filename.endsWith(".md")) {
                throw new ApiException(
                        ApiErrorCode.RAG_KNOWLEDGE_INDEX_FAILED,
                        "Only markdown files (.md) are supported");
            }

            Document document = createDocumentFromFile(filePath);
            vectorStore.add(List.of(document));

            log.info("Indexed knowledge document: {}", filename);

        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to index document: {}", filename, e);
            throw new ApiException(
                    ApiErrorCode.RAG_KNOWLEDGE_INDEX_FAILED,
                    "Failed to index document: " + filename,
                    e);
        }
    }

    @Override
    @Transactional
    public void clearKnowledgeBase() {
        try {
            log.info("Clearing knowledge base from vector store");
            vectorStoreService.deleteByMetadata("type", KNOWLEDGE_TYPE);
            log.info("Knowledge base cleared successfully");
        } catch (Exception e) {
            log.error("Failed to clear knowledge base", e);
            throw new ApiException(
                    ApiErrorCode.RAG_KNOWLEDGE_INDEX_FAILED, "Failed to clear knowledge base", e);
        }
    }

    private Document createDocumentFromFile(Path filePath) throws IOException {
        String content = Files.readString(filePath);
        String filename = filePath.getFileName().toString();

        // Extract category from subdirectory if exists
        String category = "general";
        Path parent = filePath.getParent();
        if (parent != null && !parent.endsWith(KNOWLEDGE_BASE_PATH)) {
            category = parent.getFileName().toString();
        }

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("type", KNOWLEDGE_TYPE);
        metadata.put("source", filename);
        metadata.put("category", category);
        metadata.put("documentName", filename.replace(".md", ""));

        // Generate deterministic UUID from filename for consistent re-indexing
        String documentId =
                java.util
                        .UUID
                        .nameUUIDFromBytes(
                                filename.getBytes(java.nio.charset.StandardCharsets.UTF_8))
                        .toString();

        return new Document(documentId, content, metadata);
    }
}
