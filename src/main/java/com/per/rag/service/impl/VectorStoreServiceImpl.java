package com.per.rag.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.per.common.exception.ApiErrorCode;
import com.per.common.exception.ApiException;
import com.per.product.entity.Product;
import com.per.product.entity.ProductVariant;
import com.per.product.repository.ProductRepository;
import com.per.product.repository.ProductVariantRepository;
import com.per.rag.service.VectorStoreService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class VectorStoreServiceImpl implements VectorStoreService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final VectorStore vectorStore;
    private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public void indexAllProducts() {
        try {
            log.info("Starting full product indexing for RAG");
            List<Product> products = productRepository.findAll();

            List<Document> documents = new ArrayList<>();
            for (Product product : products) {
                if (!product.isActive()) {
                    continue;
                }
                List<ProductVariant> variants =
                        productVariantRepository.findByProductId(product.getId());
                documents.add(buildDocument(product, variants));
            }

            if (!documents.isEmpty()) {
                vectorStore.add(documents);
                log.info("Indexed {} products successfully", documents.size());
            } else {
                log.warn("No active products found to index");
            }
        } catch (Exception e) {
            log.error("Failed to index products", e);
            throw new ApiException(ApiErrorCode.RAG_INDEXING_FAILED, "Failed to index products", e);
        }
    }

    @Override
    @Transactional
    public void indexProduct(UUID productId) {
        try {
            Product product =
                    productRepository
                            .findById(productId)
                            .orElseThrow(
                                    () ->
                                            new ApiException(
                                                    ApiErrorCode.PRODUCT_NOT_FOUND,
                                                    "Product not found"));

            if (!product.isActive()) {
                log.warn("Skipping inactive product: {}", productId);
                return;
            }

            List<ProductVariant> variants = productVariantRepository.findByProductId(productId);
            Document document = buildDocument(product, variants);
            vectorStore.add(List.of(document));

            log.info("Indexed product {} successfully", productId);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to index product {}", productId, e);
            throw new ApiException(
                    ApiErrorCode.RAG_INDEXING_FAILED, "Failed to index product: " + productId, e);
        }
    }

    @Override
    public List<Document> searchSimilar(String query, int topK, double threshold) {
        try {
            SearchRequest searchRequest =
                    SearchRequest.builder()
                            .query(query)
                            .topK(topK)
                            .similarityThreshold(threshold)
                            .build();

            List<Document> results = vectorStore.similaritySearch(searchRequest);
            log.debug("Found {} similar documents for query: {}", results.size(), query);
            return results;
        } catch (Exception e) {
            log.error("Failed to search similar documents", e);
            throw new ApiException(
                    ApiErrorCode.RAG_SEARCH_FAILED, "Failed to perform semantic search", e);
        }
    }

    private Document buildDocument(Product product, List<ProductVariant> variants) {
        StringBuilder content = new StringBuilder();

        // Basic info
        content.append("Product: ").append(product.getName()).append("\n");
        content.append("Brand: ").append(product.getBrand().getName()).append("\n");
        content.append("Category: ").append(product.getCategory().getName()).append("\n");
        content.append("Made in: ").append(product.getMadeIn().getName()).append("\n");

        // Description
        if (product.getShortDescription() != null && !product.getShortDescription().isBlank()) {
            content.append("Short Description: ")
                    .append(product.getShortDescription())
                    .append("\n");
        }
        if (product.getDescription() != null && !product.getDescription().isBlank()) {
            content.append("Description: ").append(product.getDescription()).append("\n");
        }

        // Perfume details
        if (product.getGender() != null) {
            content.append("Gender: ").append(product.getGender()).append("\n");
        }
        if (product.getFragranceFamily() != null) {
            content.append("Fragrance Family: ").append(product.getFragranceFamily()).append("\n");
        }
        if (product.getSillage() != null) {
            content.append("Sillage: ").append(product.getSillage()).append("\n");
        }
        if (product.getLongevity() != null) {
            content.append("Longevity: ").append(product.getLongevity()).append("\n");
        }
        if (product.getSeasonality() != null) {
            content.append("Seasonality: ").append(product.getSeasonality()).append("\n");
        }
        if (product.getOccasion() != null) {
            content.append("Occasion: ").append(product.getOccasion()).append("\n");
        }

        // Variant info
        if (!variants.isEmpty()) {
            BigDecimal minPrice =
                    variants.stream()
                            .map(ProductVariant::getPrice)
                            .min(BigDecimal::compareTo)
                            .orElse(BigDecimal.ZERO);
            BigDecimal maxPrice =
                    variants.stream()
                            .map(ProductVariant::getPrice)
                            .max(BigDecimal::compareTo)
                            .orElse(BigDecimal.ZERO);

            content.append("Price Range: ")
                    .append(minPrice)
                    .append(" - ")
                    .append(maxPrice)
                    .append(" VND\n");

            String volumes =
                    variants.stream()
                            .map(ProductVariant::getVolumeMl)
                            .filter(volume -> volume != null)
                            .distinct()
                            .map(String::valueOf)
                            .collect(Collectors.joining(", "));
            if (!volumes.isBlank()) {
                content.append("Available Volumes: ").append(volumes).append(" ml\n");
            }
        }

        // Metadata
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("productId", product.getId().toString());
        metadata.put("productName", product.getName());
        metadata.put("brandName", product.getBrand().getName());
        metadata.put("categoryName", product.getCategory().getName());

        return new Document(product.getId().toString(), content.toString(), metadata);
    }

    @Override
    @Transactional
    public void deleteByMetadata(String key, String value) {
        try {
            log.info("Deleting documents with metadata {}={}", key, value);
            String sql = "DELETE FROM vector_store WHERE metadata->>? = ?";
            int deletedCount = jdbcTemplate.update(sql, key, value);
            log.info("Deleted {} documents with metadata {}={}", deletedCount, key, value);
        } catch (Exception e) {
            log.error("Failed to delete documents by metadata", e);
            throw new ApiException(
                    ApiErrorCode.RAG_INDEXING_FAILED, "Failed to delete documents by metadata", e);
        }
    }

    @Override
    public Map<String, Object> getKnowledgeStatus() {
        try {
            Integer totalCount =
                    jdbcTemplate.queryForObject("SELECT COUNT(*) FROM vector_store", Integer.class);

            Integer knowledgeCount =
                    jdbcTemplate.queryForObject(
                            "SELECT COUNT(*) FROM vector_store WHERE metadata->>'type' = 'knowledge'",
                            Integer.class);

            List<String> sources =
                    jdbcTemplate.queryForList(
                            "SELECT DISTINCT metadata->>'source' FROM vector_store WHERE metadata->>'type' = 'knowledge'",
                            String.class);

            Map<String, Object> status = new HashMap<>();
            status.put("totalDocuments", totalCount != null ? totalCount : 0);
            status.put("knowledgeDocuments", knowledgeCount != null ? knowledgeCount : 0);
            status.put(
                    "productDocuments",
                    (totalCount != null ? totalCount : 0)
                            - (knowledgeCount != null ? knowledgeCount : 0));
            status.put("knowledgeSources", sources);
            status.put("isIndexed", knowledgeCount != null && knowledgeCount > 0);

            return status;
        } catch (Exception e) {
            log.error("Failed to get knowledge status", e);
            throw new ApiException(
                    ApiErrorCode.RAG_SEARCH_FAILED, "Failed to get knowledge status", e);
        }
    }
}
