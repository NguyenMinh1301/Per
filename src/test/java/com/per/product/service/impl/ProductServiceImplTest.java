package com.per.product.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.per.brand.entity.Brand;
import com.per.brand.repository.BrandRepository;
import com.per.category.entity.Category;
import com.per.category.repository.CategoryRepository;
import com.per.common.exception.ApiErrorCode;
import com.per.common.exception.ApiException;
import com.per.common.response.PageResponse;
import com.per.made_in.entity.MadeIn;
import com.per.made_in.repository.MadeInRepository;
import com.per.product.dto.request.ProductCreateRequest;
import com.per.product.dto.request.ProductVariantCreateRequest;
import com.per.product.dto.response.ProductDetailResponse;
import com.per.product.dto.response.ProductResponse;
import com.per.product.dto.response.ProductVariantResponse;
import com.per.product.entity.Product;
import com.per.product.entity.ProductVariant;
import com.per.product.mapper.ProductMapper;
import com.per.product.mapper.ProductVariantMapper;
import com.per.product.repository.ProductRepository;
import com.per.product.repository.ProductVariantRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService Unit Tests")
class ProductServiceImplTest {

    @Mock private ProductRepository productRepository;

    @Mock private ProductVariantRepository productVariantRepository;

    @Mock private BrandRepository brandRepository;

    @Mock private CategoryRepository categoryRepository;

    @Mock private MadeInRepository madeInRepository;

    @Mock private ProductMapper productMapper;

    @Mock private ProductVariantMapper productVariantMapper;

    @InjectMocks private ProductServiceImpl productService;

    private Product testProduct;
    private Brand testBrand;
    private Category testCategory;
    private MadeIn testMadeIn;
    private UUID productId;
    private UUID brandId;
    private UUID categoryId;
    private UUID madeInId;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        brandId = UUID.randomUUID();
        categoryId = UUID.randomUUID();
        madeInId = UUID.randomUUID();

        testBrand = Brand.builder().id(brandId).name("Test Brand").isActive(true).build();
        testCategory =
                Category.builder().id(categoryId).name("Test Category").isActive(true).build();
        testMadeIn = MadeIn.builder().id(madeInId).name("Test Country").isActive(true).build();

        testProduct =
                Product.builder()
                        .id(productId)
                        .brand(testBrand)
                        .category(testCategory)
                        .madeIn(testMadeIn)
                        .name("Test Product")
                        .isActive(true)
                        .build();
    }

    @Nested
    @DisplayName("Get Products Tests")
    class GetProductsTests {

        @Test
        @DisplayName("Should return paginated products when query is null")
        void shouldReturnPaginatedProductsWhenQueryIsNull() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Product> productPage = new PageImpl<>(java.util.List.of(testProduct));
            ProductResponse productResponse =
                    ProductResponse.builder().id(productId).name("Test Product").build();

            when(productRepository.findAll(pageable)).thenReturn(productPage);
            when(productMapper.toResponse(any(Product.class))).thenReturn(productResponse);

            // When
            PageResponse<ProductResponse> result = productService.getProducts(null, pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);

            verify(productRepository).findAll(pageable);
        }

        @Test
        @DisplayName("Should return searched products when query is provided")
        void shouldReturnSearchedProductsWhenQueryIsProvided() {
            // Given
            String query = "Test";
            Pageable pageable = PageRequest.of(0, 10);
            Page<Product> productPage = new PageImpl<>(java.util.List.of(testProduct));
            ProductResponse productResponse =
                    ProductResponse.builder().id(productId).name("Test Product").build();

            when(productRepository.findByNameContainingIgnoreCase(query, pageable))
                    .thenReturn(productPage);
            when(productMapper.toResponse(any(Product.class))).thenReturn(productResponse);

            // When
            PageResponse<ProductResponse> result = productService.getProducts(query, pageable);

            // Then
            assertThat(result).isNotNull();
            verify(productRepository).findByNameContainingIgnoreCase(query, pageable);
        }
    }

    @Nested
    @DisplayName("Get Product Tests")
    class GetProductTests {

        @Test
        @DisplayName("Should return product detail with variants")
        void shouldReturnProductDetailWithVariants() {
            // Given
            ProductVariant variant =
                    ProductVariant.builder()
                            .id(UUID.randomUUID())
                            .product(testProduct)
                            .variantSku("SKU-001")
                            .volumeMl(BigDecimal.valueOf(50))
                            .price(BigDecimal.valueOf(100000))
                            .build();

            ProductVariantResponse variantResponse =
                    ProductVariantResponse.builder()
                            .id(variant.getId())
                            .variantSku("SKU-001")
                            .volumeMl(BigDecimal.valueOf(50))
                            .price(BigDecimal.valueOf(100000))
                            .build();

            ProductDetailResponse detailResponse =
                    ProductDetailResponse.builder()
                            .id(productId)
                            .name("Test Product")
                            .variants(java.util.List.of(variantResponse))
                            .build();

            when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
            when(productVariantRepository.findByProductId(productId))
                    .thenReturn(java.util.List.of(variant));
            when(productMapper.toDetail(testProduct)).thenReturn(detailResponse);
            when(productVariantMapper.toResponse(variant)).thenReturn(variantResponse);

            // When
            ProductDetailResponse result = productService.getProduct(productId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(productId);
            assertThat(result.getVariants()).hasSize(1);

            verify(productRepository).findById(productId);
            verify(productVariantRepository).findByProductId(productId);
        }

        @Test
        @DisplayName("Should throw exception when product not found")
        void shouldThrowExceptionWhenProductNotFound() {
            // Given
            UUID nonExistentId = UUID.randomUUID();
            when(productRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> productService.getProduct(nonExistentId))
                    .isInstanceOf(ApiException.class)
                    .extracting("errorCode")
                    .isEqualTo(ApiErrorCode.PRODUCT_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("Create Product Tests")
    class CreateProductTests {

        @Test
        @DisplayName("Should create product successfully")
        void shouldCreateProductSuccessfully() {
            // Given
            ProductCreateRequest request =
                    ProductCreateRequest.builder()
                            .brandId(brandId)
                            .categoryId(categoryId)
                            .madeInId(madeInId)
                            .name("New Product")
                            .description("New Description")
                            .build();

            Product newProduct =
                    Product.builder()
                            .id(UUID.randomUUID())
                            .brand(testBrand)
                            .category(testCategory)
                            .madeIn(testMadeIn)
                            .name("New Product")
                            .description("New Description")
                            .isActive(true)
                            .build();

            ProductDetailResponse detailResponse =
                    ProductDetailResponse.builder()
                            .id(newProduct.getId())
                            .name("New Product")
                            .description("New Description")
                            .variants(new ArrayList<>())
                            .build();

            when(brandRepository.findById(brandId)).thenReturn(Optional.of(testBrand));
            when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(testCategory));
            when(madeInRepository.findById(madeInId)).thenReturn(Optional.of(testMadeIn));
            when(productRepository.existsByNameIgnoreCase("New Product")).thenReturn(false);
            when(productMapper.toEntity(request)).thenReturn(newProduct);
            when(productRepository.save(any(Product.class))).thenReturn(newProduct);
            when(productVariantRepository.findByProductId(newProduct.getId()))
                    .thenReturn(new ArrayList<>());
            when(productMapper.toDetail(newProduct)).thenReturn(detailResponse);

            // When
            ProductDetailResponse result = productService.createProduct(request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("New Product");

            verify(productRepository).existsByNameIgnoreCase("New Product");
            verify(productRepository).save(any(Product.class));
        }

        @Test
        @DisplayName("Should create product with variants successfully")
        void shouldCreateProductWithVariantsSuccessfully() {
            // Given
            ProductVariantCreateRequest variantRequest =
                    ProductVariantCreateRequest.builder()
                            .variantSku("SKU-001")
                            .volumeMl(BigDecimal.valueOf(50))
                            .price(BigDecimal.valueOf(100000))
                            .build();

            Set<ProductVariantCreateRequest> variants = new HashSet<>();
            variants.add(variantRequest);

            ProductCreateRequest request =
                    ProductCreateRequest.builder()
                            .brandId(brandId)
                            .categoryId(categoryId)
                            .madeInId(madeInId)
                            .name("New Product")
                            .variants(variants)
                            .build();

            Product newProduct =
                    Product.builder()
                            .id(UUID.randomUUID())
                            .brand(testBrand)
                            .category(testCategory)
                            .madeIn(testMadeIn)
                            .name("New Product")
                            .isActive(true)
                            .build();

            ProductVariant variant =
                    ProductVariant.builder()
                            .id(UUID.randomUUID())
                            .product(newProduct)
                            .variantSku("SKU-001")
                            .volumeMl(BigDecimal.valueOf(50))
                            .price(BigDecimal.valueOf(100000))
                            .currencyCode("VND")
                            .stockQuantity(0)
                            .lowStockThreshold(0)
                            .build();

            ProductDetailResponse detailResponse =
                    ProductDetailResponse.builder()
                            .id(newProduct.getId())
                            .name("New Product")
                            .variants(new ArrayList<>())
                            .build();

            when(brandRepository.findById(brandId)).thenReturn(Optional.of(testBrand));
            when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(testCategory));
            when(madeInRepository.findById(madeInId)).thenReturn(Optional.of(testMadeIn));
            when(productRepository.existsByNameIgnoreCase("New Product")).thenReturn(false);
            when(productMapper.toEntity(request)).thenReturn(newProduct);
            when(productRepository.save(any(Product.class))).thenReturn(newProduct);
            when(productVariantRepository.existsByVariantSkuIgnoreCase("SKU-001"))
                    .thenReturn(false);
            when(productVariantMapper.toEntity(variantRequest)).thenReturn(variant);
            when(productVariantRepository.saveAll(anyList()))
                    .thenReturn(java.util.List.of(variant));
            when(productVariantRepository.findByProductId(newProduct.getId()))
                    .thenReturn(java.util.List.of(variant));
            when(productMapper.toDetail(newProduct)).thenReturn(detailResponse);

            // When
            ProductDetailResponse result = productService.createProduct(request);

            // Then
            assertThat(result).isNotNull();
            verify(productVariantRepository).saveAll(anyList());
        }

        @Test
        @DisplayName("Should throw exception when product name already exists")
        void shouldThrowExceptionWhenProductNameExists() {
            // Given
            ProductCreateRequest request =
                    ProductCreateRequest.builder()
                            .brandId(brandId)
                            .categoryId(categoryId)
                            .madeInId(madeInId)
                            .name("Existing Product")
                            .build();

            when(brandRepository.findById(brandId)).thenReturn(Optional.of(testBrand));
            when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(testCategory));
            when(madeInRepository.findById(madeInId)).thenReturn(Optional.of(testMadeIn));
            when(productRepository.existsByNameIgnoreCase("Existing Product")).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> productService.createProduct(request))
                    .isInstanceOf(ApiException.class)
                    .extracting("errorCode")
                    .isEqualTo(ApiErrorCode.PRODUCT_NAME_CONFLICT);
        }
    }

    @Nested
    @DisplayName("Add Variant Tests")
    class AddVariantTests {

        @Test
        @DisplayName("Should add variant successfully")
        void shouldAddVariantSuccessfully() {
            // Given
            ProductVariantCreateRequest request =
                    ProductVariantCreateRequest.builder()
                            .variantSku("SKU-001")
                            .volumeMl(BigDecimal.valueOf(50))
                            .price(BigDecimal.valueOf(100000))
                            .build();

            ProductVariant variant =
                    ProductVariant.builder()
                            .id(UUID.randomUUID())
                            .product(testProduct)
                            .variantSku("SKU-001")
                            .volumeMl(BigDecimal.valueOf(50))
                            .price(BigDecimal.valueOf(100000))
                            .currencyCode("VND")
                            .stockQuantity(0)
                            .lowStockThreshold(0)
                            .build();

            ProductVariantResponse variantResponse =
                    ProductVariantResponse.builder()
                            .id(variant.getId())
                            .variantSku("SKU-001")
                            .volumeMl(BigDecimal.valueOf(50))
                            .price(BigDecimal.valueOf(100000))
                            .build();

            when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
            when(productVariantRepository.existsByVariantSkuIgnoreCase("SKU-001"))
                    .thenReturn(false);
            when(productVariantMapper.toEntity(request)).thenReturn(variant);
            when(productVariantRepository.save(any(ProductVariant.class))).thenReturn(variant);
            when(productVariantMapper.toResponse(variant)).thenReturn(variantResponse);

            // When
            ProductVariantResponse result = productService.addVariant(productId, request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getVariantSku()).isEqualTo("SKU-001");

            verify(productRepository).findById(productId);
            verify(productVariantRepository).save(any(ProductVariant.class));
        }

        @Test
        @DisplayName("Should throw exception when variant SKU already exists")
        void shouldThrowExceptionWhenVariantSkuExists() {
            // Given
            ProductVariantCreateRequest request =
                    ProductVariantCreateRequest.builder()
                            .variantSku("EXISTING-SKU")
                            .volumeMl(BigDecimal.valueOf(50))
                            .price(BigDecimal.valueOf(100000))
                            .build();

            when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
            when(productVariantRepository.existsByVariantSkuIgnoreCase("EXISTING-SKU"))
                    .thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> productService.addVariant(productId, request))
                    .isInstanceOf(ApiException.class)
                    .extracting("errorCode")
                    .isEqualTo(ApiErrorCode.PRODUCT_VARIANT_SKU_CONFLICT);
        }
    }

    @Nested
    @DisplayName("Delete Variant Tests")
    class DeleteVariantTests {

        @Test
        @DisplayName("Should delete variant successfully")
        void shouldDeleteVariantSuccessfully() {
            // Given
            UUID variantId = UUID.randomUUID();
            ProductVariant variant =
                    ProductVariant.builder()
                            .id(variantId)
                            .product(testProduct)
                            .variantSku("SKU-001")
                            .build();

            when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
            when(productVariantRepository.findByIdAndProductId(variantId, productId))
                    .thenReturn(Optional.of(variant));

            // When
            productService.deleteVariant(productId, variantId);

            // Then
            verify(productRepository).findById(productId);
            verify(productVariantRepository).findByIdAndProductId(variantId, productId);
            verify(productVariantRepository).delete(variant);
        }

        @Test
        @DisplayName("Should throw exception when variant not found")
        void shouldThrowExceptionWhenVariantNotFound() {
            // Given
            UUID variantId = UUID.randomUUID();
            when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
            when(productVariantRepository.findByIdAndProductId(variantId, productId))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> productService.deleteVariant(productId, variantId))
                    .isInstanceOf(ApiException.class)
                    .extracting("errorCode")
                    .isEqualTo(ApiErrorCode.PRODUCT_VARIANT_NOT_FOUND);
        }
    }
}
