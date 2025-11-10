package com.per.order.service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.per.common.exception.ApiErrorCode;
import com.per.common.exception.ApiException;
import com.per.order.entity.Order;
import com.per.order.entity.OrderItem;
import com.per.product.entity.ProductVariant;
import com.per.product.repository.ProductVariantRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderInventoryService {

    private final ProductVariantRepository productVariantRepository;

    @Transactional
    public void restoreStock(Order order) {
        Map<UUID, ProductVariant> variantsToUpdate = new HashMap<>();
        for (OrderItem item : order.getItems()) {
            UUID variantId = item.getProductVariant().getId();
            ProductVariant variant =
                    variantsToUpdate.computeIfAbsent(
                            variantId,
                            id ->
                                    productVariantRepository
                                            .findById(id)
                                            .orElseThrow(
                                                    () ->
                                                            new ApiException(
                                                                    ApiErrorCode
                                                                            .PRODUCT_VARIANT_NOT_FOUND,
                                                                    "Variant not found while restoring inventory")));
            int current = variant.getStockQuantity() == null ? 0 : variant.getStockQuantity();
            variant.setStockQuantity(current + item.getQuantity());
        }
        if (!variantsToUpdate.isEmpty()) {
            productVariantRepository.saveAll(variantsToUpdate.values());
        }
    }
}
