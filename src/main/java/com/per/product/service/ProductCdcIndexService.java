package com.per.product.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;
import org.springframework.stereotype.Service;

import com.per.product.document.ProductDocument;
import com.per.product.entity.Product;
import com.per.product.entity.ProductVariant;
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
@org.springframework.transaction.annotation.Transactional(readOnly = true)
public class ProductCdcIndexService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final ProductSearchRepository searchRepository;
    private final ProductDocumentMapper documentMapper;
    private final QdrantCdcService qdrantCdcService;
    private final ElasticsearchOperations elasticsearchOperations;

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

    // ========== Bulk Indexing (Optimized for Cascade Updates) ==========

    /**
     * Bulk index multiple products to Elasticsearch. Uses ES Bulk API for efficiency.
     *
     * @param products List of products (with relations already loaded)
     * @param variantsByProductId Map of productId -> variants
     */
    public void bulkIndexProducts(
            List<Product> products, Map<UUID, List<ProductVariant>> variantsByProductId) {
        if (products.isEmpty()) {
            log.debug("No products to index");
            return;
        }

        List<IndexQuery> indexQueries = new ArrayList<>();

        for (Product product : products) {
            List<ProductVariant> variants =
                    variantsByProductId.getOrDefault(product.getId(), List.of());
            ProductDocument document = documentMapper.toDocument(product, variants);

            IndexQuery indexQuery =
                    new IndexQueryBuilder().withId(document.getId()).withObject(document).build();
            indexQueries.add(indexQuery);
        }

        elasticsearchOperations.bulkIndex(indexQueries, ProductDocument.class);
        log.info("Bulk indexed {} products to Elasticsearch", products.size());

        // Qdrant indexing (still individual for now, can be optimized later)
        products.forEach(p -> qdrantCdcService.indexProduct(p.getId()));
    }

    /**
     * Re-index all products belonging to a specific brand. Called when a Brand entity is updated
     * via CDC. Uses optimized JOIN FETCH query and bulk indexing.
     */
    public void reindexProductsByBrand(UUID brandId) {
        List<Product> products = productRepository.findAllByBrandIdWithRelations(brandId);
        if (products.isEmpty()) {
            log.debug("No products found for brand {}", brandId);
            return;
        }

        log.info("Cascade re-indexing {} products for brand {}", products.size(), brandId);

        Map<UUID, List<ProductVariant>> variantsMap = fetchVariantsForProducts(products);
        bulkIndexProducts(products, variantsMap);
    }

    /**
     * Re-index all products belonging to a specific category. Called when a Category entity is
     * updated via CDC.
     */
    public void reindexProductsByCategory(UUID categoryId) {
        List<Product> products = productRepository.findAllByCategoryIdWithRelations(categoryId);
        if (products.isEmpty()) {
            log.debug("No products found for category {}", categoryId);
            return;
        }

        log.info("Cascade re-indexing {} products for category {}", products.size(), categoryId);

        Map<UUID, List<ProductVariant>> variantsMap = fetchVariantsForProducts(products);
        bulkIndexProducts(products, variantsMap);
    }

    /**
     * Re-index all products belonging to a specific MadeIn (country of origin). Called when a
     * MadeIn entity is updated via CDC.
     */
    public void reindexProductsByMadeIn(UUID madeInId) {
        List<Product> products = productRepository.findAllByMadeInIdWithRelations(madeInId);
        if (products.isEmpty()) {
            log.debug("No products found for madeIn {}", madeInId);
            return;
        }

        log.info("Cascade re-indexing {} products for madeIn {}", products.size(), madeInId);

        Map<UUID, List<ProductVariant>> variantsMap = fetchVariantsForProducts(products);
        bulkIndexProducts(products, variantsMap);
    }

    // ========== Private Helpers ==========

    /**
     * Fetches all variants for a list of products in a single query, then groups them by product
     * ID.
     */
    private Map<UUID, List<ProductVariant>> fetchVariantsForProducts(List<Product> products) {
        List<UUID> productIds = products.stream().map(Product::getId).toList();

        List<ProductVariant> allVariants = variantRepository.findByProductIdIn(productIds);

        return allVariants.stream().collect(Collectors.groupingBy(v -> v.getProduct().getId()));
    }
}
