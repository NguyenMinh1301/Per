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

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.per.common.exception.ApiErrorCode;
import com.per.common.exception.ApiException;
import com.per.rag.service.DocumentIndexService;

import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import io.qdrant.client.grpc.Points.PointStruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for indexing knowledge base documents into the knowledge_vectors collection. Separates
 * knowledge documents (policies, guides) from product vectors.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentIndexServiceImpl implements DocumentIndexService {

    private static final String KNOWLEDGE_BASE_PATH = "knowledge";
    private static final String KNOWLEDGE_TYPE = "knowledge";

    private final EmbeddingModel embeddingModel;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${spring.ai.vectorstore.qdrant.host}")
    private String qdrantHost;

    @Value("${spring.ai.vectorstore.qdrant.port}")
    private int qdrantGrpcPort;

    @Value("${app.rag.qdrant.collections.knowledge:knowledge_vectors}")
    private String knowledgeCollection;

    private int getHttpPort() {
        return qdrantGrpcPort - 1;
    }

    private String getQdrantBaseUrl() {
        return "http://" + qdrantHost + ":" + getHttpPort();
    }

    @Override
    public int indexKnowledgeBase() {
        try {
            log.info("Starting knowledge base indexing into collection: {}", knowledgeCollection);
            Path knowledgePath = Paths.get(KNOWLEDGE_BASE_PATH);

            if (!Files.exists(knowledgePath)) {
                log.warn("Knowledge base directory does not exist: {}", KNOWLEDGE_BASE_PATH);
                throw new ApiException(
                        ApiErrorCode.RAG_KNOWLEDGE_INDEX_FAILED,
                        "Knowledge base directory not found");
            }

            List<KnowledgeDoc> documents = new ArrayList<>();

            try (Stream<Path> paths = Files.walk(knowledgePath)) {
                paths.filter(Files::isRegularFile)
                        .filter(p -> p.toString().endsWith(".md"))
                        .filter(p -> !p.getFileName().toString().equals("system-prompt.txt"))
                        .forEach(
                                path -> {
                                    try {
                                        KnowledgeDoc doc = createKnowledgeDoc(path);
                                        documents.add(doc);
                                    } catch (IOException e) {
                                        log.error("Failed to read file: {}", path, e);
                                    }
                                });
            }

            if (!documents.isEmpty()) {
                indexToQdrant(documents);
                log.info(
                        "Indexed {} knowledge documents into {} successfully",
                        documents.size(),
                        knowledgeCollection);
                return documents.size();
            } else {
                log.warn("No markdown files found in knowledge base");
                return 0;
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

            KnowledgeDoc doc = createKnowledgeDoc(filePath);
            indexToQdrant(List.of(doc));

            log.info("Indexed knowledge document: {} into {}", filename, knowledgeCollection);

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
    public void clearKnowledgeBase() {
        try {
            log.info("Clearing knowledge base from collection: {}", knowledgeCollection);
            String url =
                    getQdrantBaseUrl() + "/collections/" + knowledgeCollection + "/points/delete";

            Map<String, Object> filter =
                    Map.of(
                            "filter",
                            Map.of(
                                    "must",
                                    List.of(
                                            Map.of(
                                                    "key",
                                                    "type",
                                                    "match",
                                                    Map.of("value", KNOWLEDGE_TYPE)))));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> request =
                    new HttpEntity<>(objectMapper.writeValueAsString(filter), headers);

            restTemplate.postForEntity(url, request, String.class);
            log.info("Knowledge base cleared successfully from {}", knowledgeCollection);
        } catch (Exception e) {
            log.error("Failed to clear knowledge base", e);
            throw new ApiException(
                    ApiErrorCode.RAG_KNOWLEDGE_INDEX_FAILED, "Failed to clear knowledge base", e);
        }
    }

    private void indexToQdrant(List<KnowledgeDoc> documents) throws Exception {
        try (QdrantClient client =
                new QdrantClient(
                        QdrantGrpcClient.newBuilder(qdrantHost, qdrantGrpcPort, false).build())) {

            List<PointStruct> points = new ArrayList<>();

            for (KnowledgeDoc doc : documents) {
                // Chunking logic
                List<String> chunks = splitText(doc.content);
                int totalChunks = chunks.size();

                for (int i = 0; i < totalChunks; i++) {
                    String chunkContent = chunks.get(i);
                    float[] embedding = embeddingModel.embed(chunkContent);

                    // Create deterministic UUID for chunk: filename + chunkIndex
                    String chunkId =
                            java.util
                                    .UUID
                                    .nameUUIDFromBytes(
                                            (doc.metadata.get("source") + "_" + i)
                                                    .getBytes(
                                                            java.nio.charset.StandardCharsets
                                                                    .UTF_8))
                                    .toString();

                    // Clone metadata and add chunk info
                    Map<String, Object> chunkMetadata = new HashMap<>(doc.metadata);
                    chunkMetadata.put("chunk_index", i);
                    chunkMetadata.put("total_chunks", totalChunks);
                    chunkMetadata.put("content", chunkContent); // Store chunk content in payload

                    PointStruct point =
                            PointStruct.newBuilder()
                                    .setId(
                                            io.qdrant.client.grpc.Points.PointId.newBuilder()
                                                    .setUuid(chunkId)
                                                    .build())
                                    .setVectors(
                                            io.qdrant.client.grpc.Points.Vectors.newBuilder()
                                                    .setVector(
                                                            io.qdrant.client.grpc.Points.Vector
                                                                    .newBuilder()
                                                                    .addAllData(
                                                                            toFloatList(embedding))
                                                                    .build())
                                                    .build())
                                    .putAllPayload(toPayload(chunkMetadata))
                                    .build();

                    points.add(point);
                }
            }

            if (!points.isEmpty()) {
                log.info(
                        "Generated {} chunks (vectors) for {} knowledge documents",
                        points.size(),
                        documents.size());
                // Upsert in batches if too large (optional, but good practice)
                client.upsertAsync(knowledgeCollection, points).get();
            }
        }
    }

    /**
     * Splits text into chunks of approximately 800 tokens (using characters as proxy). Simple
     * recursive strategy: split by paragraphs, then sentences. 1 token ~= 4 chars. 800 tokens ~=
     * 3200 chars.
     */
    private List<String> splitText(String text) {
        final int MAX_CHUNK_SIZE = 3200; // ~800 tokens
        final int OVERLAP = 400; // ~100 tokens

        List<String> chunks = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return chunks;
        }

        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + MAX_CHUNK_SIZE, text.length());

            if (end < text.length()) {
                // Try to find a paragraph break
                int lastParagraph = text.lastIndexOf("\n\n", end);
                if (lastParagraph > start) {
                    end = lastParagraph;
                } else {
                    // Try to find a sentence break
                    int lastSentence = text.lastIndexOf(". ", end);
                    if (lastSentence > start) {
                        end = lastSentence + 1;
                    } else {
                        // Try to find a space
                        int lastSpace = text.lastIndexOf(" ", end);
                        if (lastSpace > start) {
                            end = lastSpace;
                        }
                    }
                }
            }

            String chunk = text.substring(start, end).trim();
            if (!chunk.isEmpty()) {
                chunks.add(chunk);
            }

            if (end == text.length()) {
                break;
            }

            // Move start back for overlap, but ensure checking progress
            start = Math.max(end - OVERLAP, start + 1);
        }

        return chunks;
    }

    private KnowledgeDoc createKnowledgeDoc(Path filePath) throws IOException {
        String content = Files.readString(filePath);
        String filename = filePath.getFileName().toString();

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
        // content is now stored in chunk payload, not top-level doc metadata used for
        // ID generation
        // metadata.put("content", content); // Removed to save space in doc object,
        // added per chunk

        String documentId =
                java.util
                        .UUID
                        .nameUUIDFromBytes(
                                filename.getBytes(java.nio.charset.StandardCharsets.UTF_8))
                        .toString();

        return new KnowledgeDoc(documentId, content, metadata);
    }

    private List<Float> toFloatList(float[] arr) {
        List<Float> list = new ArrayList<>(arr.length);
        for (float f : arr) {
            list.add(f);
        }
        return list;
    }

    private Map<String, io.qdrant.client.grpc.JsonWithInt.Value> toPayload(
            Map<String, Object> metadata) {
        Map<String, io.qdrant.client.grpc.JsonWithInt.Value> payload = new HashMap<>();
        for (Map.Entry<String, Object> entry : metadata.entrySet()) {
            payload.put(
                    entry.getKey(),
                    io.qdrant.client.grpc.JsonWithInt.Value.newBuilder()
                            .setStringValue(String.valueOf(entry.getValue()))
                            .build());
        }
        return payload;
    }

    private record KnowledgeDoc(String id, String content, Map<String, Object> metadata) {}
}
