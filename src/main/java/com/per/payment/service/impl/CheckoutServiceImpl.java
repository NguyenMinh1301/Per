package com.per.payment.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.per.cart.entity.Cart;
import com.per.cart.entity.CartItem;
import com.per.cart.helper.CartHelper;
import com.per.cart.repository.CartRepository;
import com.per.common.exception.ApiErrorCode;
import com.per.common.exception.ApiException;
import com.per.order.entity.Order;
import com.per.order.entity.OrderItem;
import com.per.order.enums.OrderStatus;
import com.per.order.repository.OrderRepository;
import com.per.order.service.OrderCodeGenerator;
import com.per.payment.configuration.PayOsProperties;
import com.per.payment.dto.request.CheckoutRequest;
import com.per.payment.dto.response.CheckoutResponse;
import com.per.payment.entity.Payment;
import com.per.payment.enums.PaymentStatus;
import com.per.payment.repository.PaymentRepository;
import com.per.payment.service.CheckoutService;
import com.per.product.entity.ProductVariant;
import com.per.product.repository.ProductVariantRepository;
import com.per.user.entity.User;

import lombok.RequiredArgsConstructor;
import vn.payos.PayOS;
import vn.payos.type.CheckoutResponseData;
import vn.payos.type.ItemData;
import vn.payos.type.PaymentData;

@Service
@RequiredArgsConstructor
@Transactional
public class CheckoutServiceImpl implements CheckoutService {

    private static final int PAYOS_DESCRIPTION_LIMIT = 25;

    private final CartHelper cartHelper;
    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final ProductVariantRepository productVariantRepository;
    private final OrderCodeGenerator orderCodeGenerator;
    private final PayOS payOS;
    private final PayOsProperties payOsProperties;

    @Value("${app.base-url}")
    private String appBaseUrl;

    @Override
    public CheckoutResponse checkout(@Valid CheckoutRequest request) {
        User user = cartHelper.requireCurrentUser();
        Cart cart = cartHelper.getOrCreateActiveCart(user);
        List<CartItem> selectedItems = resolveSelectedItems(cart, request.getCartItemIds());
        if (selectedItems.isEmpty()) {
            throw new ApiException(ApiErrorCode.CART_ITEM_NOT_FOUND, "No cart items selected");
        }

        List<OrderItem> orderItems = new ArrayList<>();
        List<ProductVariant> variantsToPersist = new ArrayList<>();
        Set<UUID> touchedVariantIds = new HashSet<>();
        BigDecimal subtotal = BigDecimal.ZERO;
        int totalItems = 0;

        for (CartItem cartItem : selectedItems) {
            ProductVariant variant =
                    productVariantRepository
                            .findById(cartItem.getProductVariant().getId())
                            .orElseThrow(
                                    () ->
                                            new ApiException(
                                                    ApiErrorCode.PRODUCT_VARIANT_NOT_FOUND,
                                                    "Product variant not found"));

            int available = variant.getStockQuantity() == null ? 0 : variant.getStockQuantity();
            int quantity = cartItem.getQuantity();
            if (available < quantity) {
                throw new ApiException(
                        ApiErrorCode.CART_ITEM_OUT_OF_STOCK,
                        "Variant " + variant.getVariantSku() + " is out of stock");
            }

            BigDecimal unitPrice = variant.getPrice();
            if (unitPrice == null) {
                throw new ApiException(
                        ApiErrorCode.BAD_REQUEST,
                        "Variant " + variant.getVariantSku() + " missing price");
            }
            BigDecimal lineAmount =
                    unitPrice
                            .multiply(BigDecimal.valueOf(quantity))
                            .setScale(2, RoundingMode.HALF_UP);

            OrderItem orderItem =
                    OrderItem.builder()
                            .product(cartItem.getProduct())
                            .productVariant(variant)
                            .productName(cartItem.getProduct().getName())
                            .variantSku(variant.getVariantSku())
                            .quantity(quantity)
                            .unitPrice(unitPrice)
                            .subTotalAmount(lineAmount)
                            .currencyCode("VND")
                            .build();
            orderItems.add(orderItem);

            subtotal = subtotal.add(lineAmount);
            totalItems += quantity;

            variant.setStockQuantity(available - quantity);
            if (touchedVariantIds.add(variant.getId())) {
                variantsToPersist.add(variant);
            }
        }

        productVariantRepository.saveAll(variantsToPersist);

        Order order =
                Order.builder()
                        .user(user)
                        .orderCode(orderCodeGenerator.nextOrderCode())
                        .totalItems(totalItems)
                        .subtotalAmount(subtotal)
                        .grandTotal(subtotal)
                        .currencyCode("VND")
                        .receiverName(request.getReceiverName())
                        .receiverPhone(request.getReceiverPhone())
                        .shippingAddress(request.getShippingAddress())
                        .note(request.getNote())
                        .status(OrderStatus.PENDING_PAYMENT)
                        .build();
        for (OrderItem item : orderItems) {
            item.setOrder(order);
        }
        order.getItems().addAll(orderItems);
        order = orderRepository.save(order);

        removeItemsFromCart(cart, selectedItems);

        String description = buildPayOsDescription(order.getOrderCode());

        Payment payment =
                Payment.builder()
                        .order(order)
                        .user(user)
                        .orderCode(order.getOrderCode())
                        .amount(order.getGrandTotal())
                        .currencyCode(order.getCurrencyCode())
                        .description(description)
                        .status(PaymentStatus.PENDING)
                        .build();

        CheckoutResponseData payOsResponse = createPaymentLink(order, request, description);
        payment.setPaymentLinkId(payOsResponse.getPaymentLinkId());
        payment.setCheckoutUrl(payOsResponse.getCheckoutUrl());
        payment.setAmount(BigDecimal.valueOf(payOsResponse.getAmount()));
        if (payOsResponse.getExpiredAt() != null) {
            payment.setExpiredAt(Instant.ofEpochSecond(payOsResponse.getExpiredAt()));
        }
        payment = paymentRepository.save(payment);

        return CheckoutResponse.builder()
                .orderId(order.getId())
                .orderCode(order.getOrderCode())
                .orderStatus(order.getStatus())
                .paymentId(payment.getId())
                .paymentStatus(payment.getStatus())
                .paymentLinkId(payment.getPaymentLinkId())
                .checkoutUrl(payment.getCheckoutUrl())
                .amount(payment.getAmount())
                .build();
    }

    private CheckoutResponseData createPaymentLink(
            Order order, CheckoutRequest checkoutRequest, String description) {
        try {
            PaymentData.PaymentDataBuilder builder =
                    PaymentData.builder()
                            .orderCode(order.getOrderCode())
                            .amount(toPayOsAmount(order.getGrandTotal()))
                            .description(description)
                            .returnUrl(
                                    buildCallbackUrl(
                                            payOsProperties.getReturnPath(), order.getOrderCode()))
                            .cancelUrl(
                                    buildCallbackUrl(
                                            payOsProperties.getCancelPath(), order.getOrderCode()))
                            .buyerName(checkoutRequest.getReceiverName())
                            .buyerPhone(checkoutRequest.getReceiverPhone())
                            .buyerAddress(checkoutRequest.getShippingAddress());

            order.getItems()
                    .forEach(
                            item ->
                                    builder.item(
                                            ItemData.builder()
                                                    .name(item.getProductName())
                                                    .quantity(item.getQuantity())
                                                    .price(toPayOsAmount(item.getUnitPrice()))
                                                    .build()));
            return payOS.createPaymentLink(builder.build());
        } catch (Exception ex) {
            throw new ApiException(ApiErrorCode.PAYMENT_GATEWAY_ERROR, ex.getMessage());
        }
    }

    private String buildCallbackUrl(String path, Long orderCode) {
        String normalizedBase =
                appBaseUrl.endsWith("/")
                        ? appBaseUrl.substring(0, appBaseUrl.length() - 1)
                        : appBaseUrl;
        String normalizedPath = path.startsWith("/") ? path : "/" + path;
        return normalizedBase + normalizedPath + "?orderCode=" + orderCode;
    }

    private void removeItemsFromCart(Cart cart, List<CartItem> items) {
        Set<UUID> ids =
                items.stream().map(CartItem::getId).collect(HashSet::new, Set::add, Set::addAll);
        cart.getItems().removeIf(item -> ids.contains(item.getId()));
        cartHelper.recalculateCart(cart);
        cartRepository.save(cart);
    }

    private List<CartItem> resolveSelectedItems(Cart cart, List<UUID> selectedIds) {
        List<CartItem> currentItems = cart.getItems();
        if (currentItems == null) {
            return List.of();
        }
        if (CollectionUtils.isEmpty(selectedIds)) {
            return new ArrayList<>(currentItems);
        }
        Set<UUID> wanted = new HashSet<>(selectedIds);
        List<CartItem> filtered = new ArrayList<>();
        for (CartItem item : currentItems) {
            if (wanted.remove(item.getId())) {
                filtered.add(item);
            }
        }
        if (!wanted.isEmpty()) {
            throw new ApiException(
                    ApiErrorCode.CART_ITEM_NOT_FOUND,
                    "Some cart items do not belong to current user");
        }
        return filtered;
    }

    private int toPayOsAmount(BigDecimal amount) {
        long value = amount.setScale(0, RoundingMode.HALF_UP).longValueExact();
        return Math.toIntExact(value);
    }

    private String buildPayOsDescription(Long orderCode) {
        String base = "Order #" + orderCode;
        if (base.length() > PAYOS_DESCRIPTION_LIMIT) {
            return base.substring(0, PAYOS_DESCRIPTION_LIMIT);
        }
        return base;
    }
}
