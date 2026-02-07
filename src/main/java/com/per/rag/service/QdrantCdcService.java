package com.per.rag.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.per.common.exception.ApiErrorCode;
import com.per.common.exception.ApiException;
import com.per.rag.service.impl.EntityVectorServiceImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for CDC-triggered Qdrant vector indexing. Provides a unified interface for CDC consumers
 * to sync entities to Qdrant.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class QdrantCdcService {

    private final EntityVectorServiceImpl entityVectorService;

    // ========== Brand Operations ==========

    public void indexBrand(UUID brandId) {
        try {
            entityVectorService.indexBrand(brandId);
            log.debug("Indexed brand {} to Qdrant", brandId);
        } catch (ApiException e) {
            if (e.getErrorCode() == ApiErrorCode.BRAND_NOT_FOUND) {
                log.warn("Brand {} not found, skipping Qdrant indexing", brandId);
                return;
            }
            throw e;
        } catch (Exception e) {
            log.error("Failed to index brand {} to Qdrant: {}", brandId, e.getMessage(), e);
            throw new ApiException(ApiErrorCode.RAG_INDEXING_FAILED, "Failed to index brand", e);
        }
    }

    public void deleteBrand(UUID brandId) {
        try {
            // For now, we don't have a delete method in EntityVectorServiceImpl
            // The vector will be orphaned but won't affect search results significantly
            log.info("Brand {} deleted from PostgreSQL (Qdrant cleanup pending)", brandId);
        } catch (Exception e) {
            log.error("Failed to delete brand {} from Qdrant: {}", brandId, e.getMessage(), e);
        }
    }

    // ========== Category Operations ==========

    public void indexCategory(UUID categoryId) {
        try {
            entityVectorService.indexCategory(categoryId);
            log.debug("Indexed category {} to Qdrant", categoryId);
        } catch (ApiException e) {
            if (e.getErrorCode() == ApiErrorCode.CATEGORY_NOT_FOUND) {
                log.warn("Category {} not found, skipping Qdrant indexing", categoryId);
                return;
            }
            throw e;
        } catch (Exception e) {
            log.error("Failed to index category {} to Qdrant: {}", categoryId, e.getMessage(), e);
            throw new ApiException(ApiErrorCode.RAG_INDEXING_FAILED, "Failed to index category", e);
        }
    }

    public void deleteCategory(UUID categoryId) {
        try {
            // For now, we don't have a delete method in EntityVectorServiceImpl
            log.info("Category {} deleted from PostgreSQL (Qdrant cleanup pending)", categoryId);
        } catch (Exception e) {
            log.error(
                    "Failed to delete category {} from Qdrant: {}", categoryId, e.getMessage(), e);
        }
    }

    // ========== Product Operations ==========
    // Note: Product vectors are managed via the VectorStore (Spring AI) in
    // VectorStoreServiceImpl
    // These are placeholder methods for future integration

    public void indexProduct(UUID productId) {
        // Products use Spring AI VectorStore, not EntityVectorService
        // This is a no-op for now; product vectors are indexed via VectorStoreService
        log.debug(
                "Product {} CDC event received (VectorStore sync not implemented yet)", productId);
    }

    public void deleteProduct(UUID productId) {
        // Products use Spring AI VectorStore, not EntityVectorService
        log.debug(
                "Product {} deletion event received (VectorStore cleanup not implemented yet)",
                productId);
    }
}
