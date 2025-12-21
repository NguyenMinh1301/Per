package com.per.product.service.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.per.brand.entity.Brand;
import com.per.brand.repository.BrandRepository;
import com.per.category.entity.Category;
import com.per.category.repository.CategoryRepository;
import com.per.common.config.cache.CacheEvictionHelper;
import com.per.common.config.cache.CacheNames;
import com.per.common.config.kafka.KafkaTopicNames;
import com.per.common.event.ProductIndexEvent;
import com.per.common.exception.ApiErrorCode;
import com.per.common.exception.ApiException;
import com.per.common.response.PageResponse;
import com.per.made_in.entity.MadeIn;
import com.per.made_in.repository.MadeInRepository;
import com.per.product.dto.request.ProductCreateRequest;
import com.per.product.dto.request.ProductUpdateRequest;
import com.per.product.dto.request.ProductVariantCreateRequest;
import com.per.product.dto.request.ProductVariantUpdateRequest;
import com.per.product.dto.response.ProductDetailResponse;
import com.per.product.dto.response.ProductResponse;
import com.per.product.dto.response.ProductVariantResponse;
import com.per.product.entity.Product;
import com.per.product.entity.ProductVariant;
import com.per.product.mapper.ProductDocumentMapper;
import com.per.product.mapper.ProductMapper;
import com.per.product.mapper.ProductVariantMapper;
import com.per.product.repository.ProductRepository;
import com.per.product.repository.ProductVariantRepository;
import com.per.product.service.ProductService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final MadeInRepository madeInRepository;
    private final ProductMapper productMapper;
    private final ProductVariantMapper productVariantMapper;
    private final ProductDocumentMapper productDocumentMapper;
    private final CacheEvictionHelper cacheEvictionHelper;
    private final org.springframework.kafka.core.KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            value = CacheNames.PRODUCTS,
            key =
                    "'list:' + (#query ?: 'all') + ':p' + #pageable.pageNumber + ':s' + #pageable.pageSize",
            sync = true)
    public PageResponse<ProductResponse> getProducts(String query, Pageable pageable) {
        Page<Product> page;
        if (query == null || query.isBlank()) {
            page = productRepository.findAll(pageable);
        } else {
            page = productRepository.findByNameContainingIgnoreCase(query, pageable);
        }
        return PageResponse.from(page.map(productMapper::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheNames.PRODUCT, key = "#id", sync = true)
    public ProductDetailResponse getProduct(UUID id) {
        Product product = findProduct(id);
        List<ProductVariant> variants = productVariantRepository.findByProductId(product.getId());
        return buildDetail(product, variants);
    }

    @Override
    public ProductDetailResponse createProduct(ProductCreateRequest request) {
        Brand brand = findBrand(request.getBrandId());
        Category category = findCategory(request.getCategoryId());
        MadeIn madeIn = findMadeIn(request.getMadeInId());

        assertProductNameAvailable(request.getName());

        Product product = productMapper.toEntity(request);
        product.setBrand(brand);
        product.setCategory(category);
        product.setMadeIn(madeIn);

        Product savedProduct = productRepository.save(product);

        if (request.getVariants() != null && !request.getVariants().isEmpty()) {
            createVariants(savedProduct, request.getVariants());
        }

        List<ProductVariant> variants =
                productVariantRepository.findByProductId(savedProduct.getId());

        cacheEvictionHelper.evictAllAfterCommit(CacheNames.PRODUCTS, CacheNames.PRODUCT);

        // Publish event for Elasticsearch indexing
        publishIndexEvent(ProductIndexEvent.Action.INDEX, savedProduct, variants);

        return buildDetail(savedProduct, variants);
    }

    @Override
    public ProductDetailResponse updateProduct(UUID id, ProductUpdateRequest request) {
        Product product = findProduct(id);

        if (request.getBrandId() != null) {
            Brand brand = findBrand(request.getBrandId());
            product.setBrand(brand);
        }
        if (request.getCategoryId() != null) {
            Category category = findCategory(request.getCategoryId());
            product.setCategory(category);
        }
        if (request.getMadeInId() != null) {
            MadeIn madeIn = findMadeIn(request.getMadeInId());
            product.setMadeIn(madeIn);
        }
        if (request.getName() != null) {
            if (!request.getName().equalsIgnoreCase(product.getName())) {
                assertProductNameAvailable(request.getName(), product.getId());
            }
        }

        productMapper.updateEntity(request, product);

        Product savedProduct = productRepository.save(product);

        handleVariantUpdates(savedProduct, request);

        List<ProductVariant> variants =
                productVariantRepository.findByProductId(savedProduct.getId());

        cacheEvictionHelper.evictAllAfterCommit(CacheNames.PRODUCTS);
        cacheEvictionHelper.evictAfterCommit(CacheNames.PRODUCT, id);

        // Publish event for Elasticsearch indexing
        publishIndexEvent(ProductIndexEvent.Action.INDEX, savedProduct, variants);

        return buildDetail(savedProduct, variants);
    }

    @Override
    public void deleteProduct(UUID id) {
        Product product = findProduct(id);
        List<ProductVariant> variants = productVariantRepository.findByProductId(product.getId());
        if (!variants.isEmpty()) {
            productVariantRepository.deleteAll(variants);
        }
        productRepository.delete(product);

        cacheEvictionHelper.evictAllAfterCommit(CacheNames.PRODUCTS);
        cacheEvictionHelper.evictAfterCommit(CacheNames.PRODUCT, id);

        // Publish event for Elasticsearch deletion
        publishDeleteEvent(id);
    }

    @Override
    public ProductVariantResponse addVariant(UUID productId, ProductVariantCreateRequest request) {
        Product product = findProduct(productId);
        assertVariantSkuAvailable(request.getVariantSku());

        ProductVariant variant = productVariantMapper.toEntity(request);
        variant.setProduct(product);
        applyVariantDefaults(variant);

        ProductVariant saved = productVariantRepository.save(variant);

        cacheEvictionHelper.evictAfterCommit(CacheNames.PRODUCT, productId);

        return productVariantMapper.toResponse(saved);
    }

    @Override
    public ProductVariantResponse updateVariant(
            UUID productId, UUID variantId, ProductVariantUpdateRequest request) {
        Product product = findProduct(productId);
        ProductVariant variant = findVariant(product.getId(), variantId);

        if (request.getVariantSku() != null
                && !request.getVariantSku().equalsIgnoreCase(variant.getVariantSku())) {
            assertVariantSkuAvailable(request.getVariantSku(), variant.getId());
        }

        productVariantMapper.updateEntity(request, variant);
        applyVariantDefaults(variant);

        ProductVariant saved = productVariantRepository.save(variant);

        cacheEvictionHelper.evictAfterCommit(CacheNames.PRODUCT, productId);

        return productVariantMapper.toResponse(saved);
    }

    @Override
    public void deleteVariant(UUID productId, UUID variantId) {
        Product product = findProduct(productId);
        ProductVariant variant = findVariant(product.getId(), variantId);
        productVariantRepository.delete(variant);

        cacheEvictionHelper.evictAfterCommit(CacheNames.PRODUCT, productId);
    }

    private ProductDetailResponse buildDetail(Product product, List<ProductVariant> variants) {
        ProductDetailResponse detail = productMapper.toDetail(product);
        List<ProductVariantResponse> variantResponses = new ArrayList<>();
        for (ProductVariant variant : variants) {
            variantResponses.add(productVariantMapper.toResponse(variant));
        }
        detail.setVariants(variantResponses);
        return detail;
    }

    private void handleVariantUpdates(Product product, ProductUpdateRequest request) {
        if (request.getVariantsToAdd() != null && !request.getVariantsToAdd().isEmpty()) {
            createVariants(product, request.getVariantsToAdd());
        }

        if (request.getVariantsToUpdate() != null && !request.getVariantsToUpdate().isEmpty()) {
            for (ProductVariantUpdateRequest updateRequest : request.getVariantsToUpdate()) {
                if (updateRequest.getId() == null) {
                    continue;
                }
                ProductVariant variant = findVariant(product.getId(), updateRequest.getId());

                if (updateRequest.getVariantSku() != null
                        && !updateRequest
                                .getVariantSku()
                                .equalsIgnoreCase(variant.getVariantSku())) {
                    assertVariantSkuAvailable(updateRequest.getVariantSku(), variant.getId());
                }

                productVariantMapper.updateEntity(updateRequest, variant);
                applyVariantDefaults(variant);
                productVariantRepository.save(variant);
            }
        }

        if (request.getVariantsToDelete() != null && !request.getVariantsToDelete().isEmpty()) {
            for (UUID variantId : request.getVariantsToDelete()) {
                ProductVariant variant = findVariant(product.getId(), variantId);
                productVariantRepository.delete(variant);
            }
        }
    }

    private void createVariants(Product product, Set<ProductVariantCreateRequest> requests) {
        Set<String> seenSku = new HashSet<>();
        List<ProductVariant> variants = new ArrayList<>();

        for (ProductVariantCreateRequest request : requests) {
            String skuLower = request.getVariantSku().toLowerCase();
            boolean duplicateInPayload = !seenSku.add(skuLower);
            if (duplicateInPayload) {
                throw new ApiException(
                        ApiErrorCode.PRODUCT_VARIANT_SKU_CONFLICT,
                        "Variant SKU '"
                                + request.getVariantSku()
                                + "' appears multiple times in request");
            }

            assertVariantSkuAvailable(request.getVariantSku());

            ProductVariant variant = productVariantMapper.toEntity(request);
            variant.setProduct(product);
            applyVariantDefaults(variant);
            variants.add(variant);
        }

        productVariantRepository.saveAll(variants);
    }

    private void applyVariantDefaults(ProductVariant variant) {
        if (variant.getCurrencyCode() == null || variant.getCurrencyCode().isBlank()) {
            variant.setCurrencyCode("VND");
        }
        if (variant.getStockQuantity() == null) {
            variant.setStockQuantity(0);
        }
        if (variant.getLowStockThreshold() == null) {
            variant.setLowStockThreshold(0);
        }
    }

    private Product findProduct(UUID id) {
        return require(
                productRepository.findById(id),
                ApiErrorCode.PRODUCT_NOT_FOUND,
                "Product not found");
    }

    private ProductVariant findVariant(UUID productId, UUID variantId) {
        return require(
                productVariantRepository.findByIdAndProductId(variantId, productId),
                ApiErrorCode.PRODUCT_VARIANT_NOT_FOUND,
                "Product variant not found");
    }

    private Brand findBrand(UUID id) {
        return require(
                brandRepository.findById(id), ApiErrorCode.BRAND_NOT_FOUND, "Brand not found");
    }

    private Category findCategory(UUID id) {
        return require(
                categoryRepository.findById(id),
                ApiErrorCode.CATEGORY_NOT_FOUND,
                "Category not found");
    }

    private MadeIn findMadeIn(UUID id) {
        return require(
                madeInRepository.findById(id),
                ApiErrorCode.MADEIN_NOT_FOUND,
                "Made in origin not found");
    }

    private void assertProductNameAvailable(String name) {
        if (productRepository.existsByNameIgnoreCase(name)) {
            throw new ApiException(
                    ApiErrorCode.PRODUCT_NAME_CONFLICT, "Product name already exists");
        }
    }

    private void assertProductNameAvailable(String name, UUID excludeId) {
        if (productRepository.existsByNameIgnoreCaseAndIdNot(name, excludeId)) {
            throw new ApiException(
                    ApiErrorCode.PRODUCT_NAME_CONFLICT, "Product name already exists");
        }
    }

    private void assertVariantSkuAvailable(String sku) {
        if (productVariantRepository.existsByVariantSkuIgnoreCase(sku)) {
            throw new ApiException(
                    ApiErrorCode.PRODUCT_VARIANT_SKU_CONFLICT, "Variant SKU already exists");
        }
    }

    private void assertVariantSkuAvailable(String sku, UUID excludeId) {
        if (productVariantRepository.existsByVariantSkuIgnoreCaseAndIdNot(sku, excludeId)) {
            throw new ApiException(
                    ApiErrorCode.PRODUCT_VARIANT_SKU_CONFLICT, "Variant SKU already exists");
        }
    }

    private <T> T require(Optional<T> optional, ApiErrorCode errorCode, String message) {
        return optional.orElseThrow(() -> new ApiException(errorCode, message));
    }

    // --- Elasticsearch Indexing Helpers ---

    private void publishIndexEvent(
            ProductIndexEvent.Action action, Product product, List<ProductVariant> variants) {
        var document = productDocumentMapper.toDocument(product, variants);
        var event =
                ProductIndexEvent.builder()
                        .action(action)
                        .productId(product.getId().toString())
                        .document(document)
                        .build();
        kafkaTemplate.send(KafkaTopicNames.PRODUCT_INDEX_TOPIC, event);
    }

    private void publishDeleteEvent(UUID productId) {
        var event =
                ProductIndexEvent.builder()
                        .action(ProductIndexEvent.Action.DELETE)
                        .productId(productId.toString())
                        .build();
        kafkaTemplate.send(KafkaTopicNames.PRODUCT_INDEX_TOPIC, event);
    }
}
