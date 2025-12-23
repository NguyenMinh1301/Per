package com.per.product.mapper;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Component;

import com.per.product.document.ProductDocument;
import com.per.product.entity.Product;
import com.per.product.entity.ProductVariant;

/**
 * Maps Product entity to ProductDocument for Elasticsearch indexing. Denormalizes related entities
 * (Brand, Category) and computes aggregated fields (minPrice, maxPrice).
 */
@Component
public class ProductDocumentMapper {

    /**
     * Converts a Product entity with its variants to an Elasticsearch document.
     *
     * @param product the product entity
     * @param variants the product variants (for price range calculation)
     * @return the Elasticsearch document
     */
    public ProductDocument toDocument(Product product, List<ProductVariant> variants) {
        Double minPrice = null;
        Double maxPrice = null;

        if (variants != null && !variants.isEmpty()) {
            minPrice =
                    variants.stream()
                            .map(ProductVariant::getPrice)
                            .filter(p -> p != null)
                            .map(BigDecimal::doubleValue)
                            .min(Double::compareTo)
                            .orElse(null);

            maxPrice =
                    variants.stream()
                            .map(ProductVariant::getPrice)
                            .filter(p -> p != null)
                            .map(BigDecimal::doubleValue)
                            .max(Double::compareTo)
                            .orElse(null);
        }

        return ProductDocument.builder()
                .id(product.getId().toString())
                .name(product.getName())
                .shortDescription(product.getShortDescription())
                .description(product.getDescription())
                .brandId(product.getBrand().getId().toString())
                .brandName(product.getBrand().getName())
                .categoryId(product.getCategory().getId().toString())
                .categoryName(product.getCategory().getName())
                .gender(product.getGender() != null ? product.getGender().name() : null)
                .fragranceFamily(
                        product.getFragranceFamily() != null
                                ? product.getFragranceFamily().name()
                                : null)
                .sillage(product.getSillage() != null ? product.getSillage().name() : null)
                .longevity(product.getLongevity() != null ? product.getLongevity().name() : null)
                .seasonality(
                        product.getSeasonality() != null ? product.getSeasonality().name() : null)
                .occasion(product.getOccasion() != null ? product.getOccasion().name() : null)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .isActive(product.isActive())
                .imageUrl(product.getImageUrl())
                .build();
    }
}
