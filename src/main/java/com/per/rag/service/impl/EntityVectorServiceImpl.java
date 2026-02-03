package com.per.rag.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.per.brand.entity.Brand;
import com.per.brand.repository.BrandRepository;
import com.per.category.entity.Category;
import com.per.category.repository.CategoryRepository;
import com.per.common.exception.ApiErrorCode;
import com.per.common.exception.ApiException;
import com.per.rag.service.EntityVectorService;

import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import io.qdrant.client.grpc.Points.PointStruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** Implementation of EntityVectorService for indexing Brand and Category entities into Qdrant. */
@Service
@RequiredArgsConstructor
@Slf4j
public class EntityVectorServiceImpl implements EntityVectorService {

    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final EmbeddingModel embeddingModel;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${spring.ai.vectorstore.qdrant.host}")
    private String qdrantHost;

    @Value("${spring.ai.vectorstore.qdrant.port}")
    private int qdrantGrpcPort;

    @Value("${app.rag.qdrant.collections.brand:brand_vectors}")
    private String brandCollection;

    @Value("${app.rag.qdrant.collections.category:category_vectors}")
    private String categoryCollection;

    private int getHttpPort() {
        return qdrantGrpcPort - 1;
    }

    private String getQdrantBaseUrl() {
        return "http://" + qdrantHost + ":" + getHttpPort();
    }

    @Override
    public void indexAllBrands() {
        try {
            List<Brand> brands = brandRepository.findAll();
            log.info("Indexing {} brands into collection: {}", brands.size(), brandCollection);

            try (QdrantClient client =
                    new QdrantClient(
                            QdrantGrpcClient.newBuilder(qdrantHost, qdrantGrpcPort, false)
                                    .build())) {

                for (Brand brand : brands) {
                    if (!Boolean.TRUE.equals(brand.getIsActive())) {
                        continue;
                    }
                    indexBrandInternal(client, brand);
                }
            }

            log.info("Successfully indexed {} active brands", brands.size());
        } catch (Exception e) {
            log.error("Failed to index brands: {}", e.getMessage(), e);
            throw new ApiException(ApiErrorCode.RAG_INDEXING_FAILED, "Failed to index brands", e);
        }
    }

    @Override
    public void indexAllCategories() {
        try {
            List<Category> categories = categoryRepository.findAll();
            log.info(
                    "Indexing {} categories into collection: {}",
                    categories.size(),
                    categoryCollection);

            try (QdrantClient client =
                    new QdrantClient(
                            QdrantGrpcClient.newBuilder(qdrantHost, qdrantGrpcPort, false)
                                    .build())) {

                for (Category category : categories) {
                    if (!Boolean.TRUE.equals(category.getIsActive())) {
                        continue;
                    }
                    indexCategoryInternal(client, category);
                }
            }

            log.info("Successfully indexed {} active categories", categories.size());
        } catch (Exception e) {
            log.error("Failed to index categories: {}", e.getMessage(), e);
            throw new ApiException(
                    ApiErrorCode.RAG_INDEXING_FAILED, "Failed to index categories", e);
        }
    }

    @Override
    public void indexBrand(UUID brandId) {
        try {
            Brand brand =
                    brandRepository
                            .findById(brandId)
                            .orElseThrow(
                                    () ->
                                            new ApiException(
                                                    ApiErrorCode.BRAND_NOT_FOUND,
                                                    "Brand not found"));

            try (QdrantClient client =
                    new QdrantClient(
                            QdrantGrpcClient.newBuilder(qdrantHost, qdrantGrpcPort, false)
                                    .build())) {
                indexBrandInternal(client, brand);
            }

            log.info("Successfully indexed brand: {}", brand.getName());
        } catch (Exception e) {
            log.error("Failed to index brand {}: {}", brandId, e.getMessage(), e);
            throw new ApiException(ApiErrorCode.RAG_INDEXING_FAILED, "Failed to index brand", e);
        }
    }

    @Override
    public void indexCategory(UUID categoryId) {
        try {
            Category category =
                    categoryRepository
                            .findById(categoryId)
                            .orElseThrow(
                                    () ->
                                            new ApiException(
                                                    ApiErrorCode.CATEGORY_NOT_FOUND,
                                                    "Category not found"));

            try (QdrantClient client =
                    new QdrantClient(
                            QdrantGrpcClient.newBuilder(qdrantHost, qdrantGrpcPort, false)
                                    .build())) {
                indexCategoryInternal(client, category);
            }

            log.info("Successfully indexed category: {}", category.getName());
        } catch (Exception e) {
            log.error("Failed to index category {}: {}", categoryId, e.getMessage(), e);
            throw new ApiException(ApiErrorCode.RAG_INDEXING_FAILED, "Failed to index category", e);
        }
    }

    @Override
    public Map<String, Object> getCollectionStatus(String collectionName) {
        try {
            String url = getQdrantBaseUrl() + "/collections/" + collectionName;
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode result = root.path("result");

            Map<String, Object> status = new HashMap<>();
            status.put("collection", collectionName);
            status.put("vectorsCount", result.path("vectors_count").asInt(0));
            status.put("pointsCount", result.path("points_count").asInt(0));
            status.put("status", result.path("status").asText("unknown"));

            return status;
        } catch (Exception e) {
            log.warn("Failed to get status for collection {}: {}", collectionName, e.getMessage());
            Map<String, Object> status = new HashMap<>();
            status.put("collection", collectionName);
            status.put("vectorsCount", 0);
            status.put("pointsCount", 0);
            status.put("status", "error");
            status.put("error", e.getMessage());
            return status;
        }
    }

    @Override
    public boolean isCollectionEmpty(String collectionName) {
        Map<String, Object> status = getCollectionStatus(collectionName);
        int pointsCount = (Integer) status.getOrDefault("pointsCount", 0);
        return pointsCount == 0;
    }

    private void indexBrandInternal(QdrantClient client, Brand brand) throws Exception {
        String content = buildBrandContent(brand);
        float[] embedding = embeddingModel.embed(content);

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("type", "brand");
        metadata.put("id", brand.getId().toString());
        metadata.put("name", brand.getName());

        PointStruct point =
                PointStruct.newBuilder()
                        .setId(
                                io.qdrant.client.grpc.Points.PointId.newBuilder()
                                        .setUuid(brand.getId().toString())
                                        .build())
                        .setVectors(
                                io.qdrant.client.grpc.Points.Vectors.newBuilder()
                                        .setVector(
                                                io.qdrant.client.grpc.Points.Vector.newBuilder()
                                                        .addAllData(toFloatList(embedding))
                                                        .build())
                                        .build())
                        .putAllPayload(toPayload(metadata))
                        .build();

        client.upsertAsync(brandCollection, List.of(point)).get();
    }

    private void indexCategoryInternal(QdrantClient client, Category category) throws Exception {
        String content = buildCategoryContent(category);
        float[] embedding = embeddingModel.embed(content);

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("type", "category");
        metadata.put("id", category.getId().toString());
        metadata.put("name", category.getName());

        PointStruct point =
                PointStruct.newBuilder()
                        .setId(
                                io.qdrant.client.grpc.Points.PointId.newBuilder()
                                        .setUuid(category.getId().toString())
                                        .build())
                        .setVectors(
                                io.qdrant.client.grpc.Points.Vectors.newBuilder()
                                        .setVector(
                                                io.qdrant.client.grpc.Points.Vector.newBuilder()
                                                        .addAllData(toFloatList(embedding))
                                                        .build())
                                        .build())
                        .putAllPayload(toPayload(metadata))
                        .build();

        client.upsertAsync(categoryCollection, List.of(point)).get();
    }

    private String buildBrandContent(Brand brand) {
        StringBuilder sb = new StringBuilder();
        sb.append("Brand: ").append(brand.getName());
        if (brand.getDescription() != null && !brand.getDescription().isBlank()) {
            sb.append(". ").append(brand.getDescription());
        }
        if (brand.getFoundedYear() != null) {
            sb.append(". Founded in ").append(brand.getFoundedYear());
        }
        return sb.toString();
    }

    private String buildCategoryContent(Category category) {
        StringBuilder sb = new StringBuilder();
        sb.append("Category: ").append(category.getName());
        if (category.getDescription() != null && !category.getDescription().isBlank()) {
            sb.append(". ").append(category.getDescription());
        }
        if (category.getDescriptions() != null && !category.getDescriptions().isBlank()) {
            sb.append(" ").append(category.getDescriptions());
        }
        return sb.toString();
    }

    private List<Float> toFloatList(float[] arr) {
        List<Float> list = new java.util.ArrayList<>(arr.length);
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
}
