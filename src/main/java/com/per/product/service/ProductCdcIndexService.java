package com.per.product.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.per.product.entity.Product;
import com.per.product.mapper.ProductDocumentMapper;
import com.per.product.repository.ProductRepository;
import com.per.product.repository.ProductSearchRepository;
import com.per.product.repository.ProductVariantRepository;
import com.per.rag.service.QdrantCdcService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for CDC-triggered Product indexing to Elasticsearch and Qdrant. Provides methods for
 * direct indexing and cascade re-indexing.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@org.springframework.transaction.annotation.Transactional
public class ProductCdcIndexService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final ProductSearchRepository searchRepository;
    private final ProductDocumentMapper documentMapper;
    private final QdrantCdcService qdrantCdcService;

    /** Index a single product to Elasticsearch and Qdrant. */
    public void indexProduct(UUID productId) {
        productRepository
                .findById(productId)
                .ifPresentOrElse(
                        product -> {
                            var variants = variantRepository.findByProductId(productId);
                            var document = documentMapper.toDocument(product, variants);
                            searchRepository.save(document);
                            qdrantCdcService.indexProduct(productId);
                            log.debug("Indexed product {} to ES/Qdrant", productId);
                        },
                        () -> log.warn("Product {} not found, skipping indexing", productId));
    }

    /** Delete a product from Elasticsearch and Qdrant. */
    public void deleteProduct(UUID productId) {
        try {
            searchRepository.deleteById(productId.toString());
            qdrantCdcService.deleteProduct(productId);
            log.debug("Deleted product {} from ES/Qdrant", productId);
        } catch (Exception e) {
            log.error(
                    "Failed to delete product {} from ES/Qdrant: {}", productId, e.getMessage(), e);
        }
    }

    /**
     * Re-index all products belonging to a specific brand. Called when a Brand entity is updated
     * via CDC.
     */
    public void reindexProductsByBrand(UUID brandId) {
        List<Product> products = productRepository.findByBrandId(brandId);
        log.info("Cascade re-indexing {} products for brand {}", products.size(), brandId);

        for (Product product : products) {
            indexProduct(product.getId());
        }
    }

    /**
     * Re-index all products belonging to a specific category. Called when a Category entity is
     * updated via CDC.
     */
    public void reindexProductsByCategory(UUID categoryId) {
        List<Product> products = productRepository.findByCategoryId(categoryId);
        log.info("Cascade re-indexing {} products for category {}", products.size(), categoryId);

        for (Product product : products) {
            indexProduct(product.getId());
        }
    }

    /**
     * Re-index all products belonging to a specific MadeIn (country of origin). Called when a
     * MadeIn entity is updated via CDC.
     */
    public void reindexProductsByMadeIn(UUID madeInId) {
        List<Product> products = productRepository.findByMadeInId(madeInId);
        log.info("Cascade re-indexing {} products for madeIn {}", products.size(), madeInId);

        for (Product product : products) {
            indexProduct(product.getId());
        }
    }
}
