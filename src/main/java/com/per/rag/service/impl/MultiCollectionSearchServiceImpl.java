package com.per.rag.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.per.rag.service.MultiCollectionSearchService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of multi-collection search across Qdrant. Searches brand, category, product, and
 * knowledge collections independently.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MultiCollectionSearchServiceImpl implements MultiCollectionSearchService {

    private final EmbeddingModel embeddingModel;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${spring.ai.vectorstore.qdrant.host:qdrant}")
    private String qdrantHost;

    @Value("${spring.ai.vectorstore.qdrant.port:6334}")
    private int qdrantGrpcPort;

    @Value("${app.rag.qdrant.collections.product:product_vectors}")
    private String productCollection;

    @Value("${app.rag.qdrant.collections.brand:brand_vectors}")
    private String brandCollection;

    @Value("${app.rag.qdrant.collections.category:category_vectors}")
    private String categoryCollection;

    @Value("${app.rag.qdrant.collections.knowledge:knowledge_vectors}")
    private String knowledgeCollection;

    private String getQdrantBaseUrl() {
        return "http://" + qdrantHost + ":6333";
    }

    @Override
    public List<Document> searchBrands(String query, int topK, double threshold) {
        return searchCollection(brandCollection, query, topK, threshold);
    }

    @Override
    public List<Document> searchCategories(String query, int topK, double threshold) {
        return searchCollection(categoryCollection, query, topK, threshold);
    }

    @Override
    public List<Document> searchProducts(String query, int topK, double threshold) {
        return searchCollection(productCollection, query, topK, threshold);
    }

    @Override
    public List<Document> searchKnowledge(String query, int topK, double threshold) {
        return searchCollection(knowledgeCollection, query, topK, threshold);
    }

    @Override
    public MultiSearchResult searchAll(String query, int topK, double threshold) {
        List<Document> brands = searchBrands(query, topK, threshold);
        List<Document> categories = searchCategories(query, topK, threshold);
        List<Document> products = searchProducts(query, topK, threshold);
        List<Document> knowledge = searchKnowledge(query, topK, threshold);

        log.debug(
                "Multi-search results - brands:{}, categories:{}, products:{}, knowledge:{}",
                brands.size(),
                categories.size(),
                products.size(),
                knowledge.size());

        return new MultiSearchResult(brands, categories, products, knowledge);
    }

    private List<Document> searchCollection(
            String collectionName, String query, int topK, double threshold) {
        List<Document> results = new ArrayList<>();

        try {
            float[] queryVector = embeddingModel.embed(query);

            String url = getQdrantBaseUrl() + "/collections/" + collectionName + "/points/search";

            Map<String, Object> searchRequest = new HashMap<>();
            searchRequest.put("vector", toFloatList(queryVector));
            searchRequest.put("limit", topK);
            searchRequest.put("score_threshold", threshold);
            searchRequest.put("with_payload", true);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> request =
                    new HttpEntity<>(objectMapper.writeValueAsString(searchRequest), headers);

            ResponseEntity<String> response =
                    restTemplate.postForEntity(url, request, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode points = root.path("result");

                if (points.isArray()) {
                    for (JsonNode point : points) {
                        String id = extractId(point);
                        double score = point.path("score").asDouble(0);
                        JsonNode payload = point.path("payload");

                        Map<String, Object> metadata = new HashMap<>();
                        metadata.put("score", score);
                        metadata.put("collection", collectionName);

                        payload.fields()
                                .forEachRemaining(
                                        field ->
                                                metadata.put(
                                                        field.getKey(), field.getValue().asText()));

                        String content = payload.path("content").asText("");
                        if (content.isEmpty()) {
                            content = buildContentFromPayload(payload);
                        }

                        results.add(new Document(id, content, metadata));
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Failed to search collection {}: {}", collectionName, e.getMessage());
        }

        return results;
    }

    private String extractId(JsonNode point) {
        JsonNode idNode = point.path("id");
        if (idNode.isTextual()) {
            return idNode.asText();
        } else if (idNode.isNumber()) {
            return String.valueOf(idNode.asLong());
        }
        return idNode.toString();
    }

    private String buildContentFromPayload(JsonNode payload) {
        StringBuilder sb = new StringBuilder();
        payload.fields()
                .forEachRemaining(
                        field -> {
                            if (!field.getKey().equals("content")) {
                                sb.append(field.getKey())
                                        .append(": ")
                                        .append(field.getValue().asText())
                                        .append("\n");
                            }
                        });
        return sb.toString();
    }

    private List<Float> toFloatList(float[] arr) {
        List<Float> list = new ArrayList<>(arr.length);
        for (float f : arr) {
            list.add(f);
        }
        return list;
    }
}
