package com.per.cart.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.per.cart.dto.request.CartItemCreateRequest;
import com.per.cart.dto.request.CartItemUpdateRequest;
import com.per.cart.dto.response.CartResponse;
import com.per.cart.entity.Cart;
import com.per.cart.entity.CartItem;
import com.per.cart.helper.CartHelper;
import com.per.cart.mapper.CartMapper;
import com.per.cart.repository.CartItemRepository;
import com.per.cart.repository.CartRepository;
import com.per.cart.service.CartItemService;
import com.per.common.exception.ApiErrorCode;
import com.per.common.exception.ApiException;
import com.per.product.entity.Product;
import com.per.product.entity.ProductVariant;
import com.per.product.repository.ProductVariantRepository;
import com.per.user.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class CartItemServiceImpl implements CartItemService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductVariantRepository productVariantRepository;
    private final CartMapper cartMapper;
    private final CartHelper cartHelper;

    @Override
    public CartResponse addItem(CartItemCreateRequest request) {
        User user = cartHelper.requireCurrentUser();
        Cart cart = cartHelper.getOrCreateActiveCart(user);

        ProductVariant variant = getActiveVariant(request.getVariantId());
        Product product = variant.getProduct();
        assertProductActive(product);

        int quantityToAdd = request.getQuantity();
        CartItem cartItem =
                cartItemRepository
                        .findByCartIdAndProductVariantId(cart.getId(), variant.getId())
                        .orElse(null);

        if (cartItem != null) {
            int updatedQuantity = cartItem.getQuantity() + quantityToAdd;
            assertStockAvailable(variant, updatedQuantity);
            cartItem.setQuantity(updatedQuantity);
            cartItem.setPrice(variant.getPrice());
            cartItem.setProductVariant(variant);
            cartItem.setProduct(product);
            cartItem.setSubTotalAmount(calculateSubTotal(variant.getPrice(), updatedQuantity));
        } else {
            assertStockAvailable(variant, quantityToAdd);
            CartItem newItem =
                    CartItem.builder()
                            .cart(cart)
                            .product(product)
                            .productVariant(variant)
                            .quantity(quantityToAdd)
                            .price(variant.getPrice())
                            .subTotalAmount(calculateSubTotal(variant.getPrice(), quantityToAdd))
                            .build();
            cart.getItems().add(newItem);
        }

        cartHelper.recalculateCart(cart);
        Cart savedCart = cartRepository.save(cart);
        return cartMapper.toResponse(savedCart);
    }

    @Override
    public CartResponse updateItem(UUID itemId, CartItemUpdateRequest request) {
        User user = cartHelper.requireCurrentUser();
        CartItem cartItem = getCartItemForUser(user, itemId);
        Cart cart = cartItem.getCart();

        ProductVariant variant = getActiveVariant(cartItem.getProductVariant().getId());
        assertProductActive(variant.getProduct());

        int newQuantity = request.getQuantity();
        assertStockAvailable(variant, newQuantity);

        cartItem.setQuantity(newQuantity);
        cartItem.setPrice(variant.getPrice());
        cartItem.setProductVariant(variant);
        cartItem.setProduct(variant.getProduct());
        cartItem.setSubTotalAmount(calculateSubTotal(variant.getPrice(), newQuantity));

        cartHelper.recalculateCart(cart);
        Cart savedCart = cartRepository.save(cart);
        return cartMapper.toResponse(savedCart);
    }

    @Override
    public CartResponse removeItem(UUID itemId) {
        User user = cartHelper.requireCurrentUser();
        CartItem cartItem = getCartItemForUser(user, itemId);
        Cart cart = cartItem.getCart();

        cart.getItems().remove(cartItem);
        cartItem.setCart(null);

        cartHelper.recalculateCart(cart);
        Cart savedCart = cartRepository.save(cart);
        return cartMapper.toResponse(savedCart);
    }

    @Override
    public CartResponse clearCart() {
        User user = cartHelper.requireCurrentUser();
        Cart cart = cartHelper.getOrCreateActiveCart(user);
        cart.getItems().clear();
        cart.setDiscountAmount(BigDecimal.ZERO);
        cartHelper.recalculateCart(cart);
        Cart savedCart = cartRepository.save(cart);
        return cartMapper.toResponse(savedCart);
    }

    private CartItem getCartItemForUser(User user, UUID itemId) {
        CartItem cartItem =
                cartItemRepository
                        .findById(itemId)
                        .orElseThrow(
                                () ->
                                        new ApiException(
                                                ApiErrorCode.CART_ITEM_NOT_FOUND,
                                                "Cart item not found"));
        if (!cartItem.getCart().getUser().getId().equals(user.getId())) {
            throw new ApiException(ApiErrorCode.CART_ITEM_NOT_FOUND, "Cart item not found");
        }
        return cartItem;
    }

    private ProductVariant getActiveVariant(UUID variantId) {
        ProductVariant variant =
                productVariantRepository
                        .findById(variantId)
                        .orElseThrow(
                                () ->
                                        new ApiException(
                                                ApiErrorCode.PRODUCT_VARIANT_NOT_FOUND,
                                                "Product variant not found"));
        if (!variant.isActive()) {
            throw new ApiException(
                    ApiErrorCode.PRODUCT_VARIANT_NOT_FOUND, "Product variant is inactive");
        }
        return variant;
    }

    private void assertProductActive(Product product) {
        if (product == null || !product.isActive()) {
            throw new ApiException(
                    ApiErrorCode.PRODUCT_NOT_FOUND, "Product is inactive or not available");
        }
    }

    private void assertStockAvailable(ProductVariant variant, int desiredQuantity) {
        Integer stock = variant.getStockQuantity();
        if (stock != null && stock >= 0 && desiredQuantity > stock) {
            throw new ApiException(
                    ApiErrorCode.CART_ITEM_OUT_OF_STOCK,
                    "Requested quantity exceeds available stock");
        }
    }

    private BigDecimal calculateSubTotal(BigDecimal price, int quantity) {
        return price.multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP);
    }
}
