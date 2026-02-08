package com.per.cart.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.per.auth.repository.UserRepository;
import com.per.auth.security.principal.UserPrincipal;
import com.per.cart.dto.request.CartItemCreateRequest;
import com.per.cart.dto.request.CartItemUpdateRequest;
import com.per.cart.dto.response.CartResponse;
import com.per.cart.entity.Cart;
import com.per.cart.entity.CartItem;
import com.per.cart.enums.CartStatus;
import com.per.cart.helper.CartHelper;
import com.per.cart.mapper.CartMapper;
import com.per.cart.repository.CartItemRepository;
import com.per.cart.repository.CartRepository;
import com.per.cart.service.CartItemService;
import com.per.common.exception.ApiException;
import com.per.product.entity.Product;
import com.per.product.entity.ProductVariant;
import com.per.product.repository.ProductVariantRepository;
import com.per.user.entity.User;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("CartItemServiceImpl Unit Tests")
class CartItemServiceImplTest {

    @Mock private CartRepository cartRepository;
    @Mock private CartItemRepository cartItemRepository;
    @Mock private ProductVariantRepository productVariantRepository;
    @Mock private UserRepository userRepository;
    @Mock private CartMapper cartMapper;

    private CartHelper cartHelper;
    private CartItemService cartItemService;
    private ArgumentCaptor<Cart> cartCaptor;

    private User user;
    private UUID userId;

    @BeforeEach
    void setUp() {
        cartHelper = new CartHelper(cartRepository, userRepository);
        cartItemService =
                new CartItemServiceImpl(
                        cartRepository,
                        cartItemRepository,
                        productVariantRepository,
                        cartMapper,
                        cartHelper);

        userId = UUID.randomUUID();
        user =
                User.builder()
                        .id(userId)
                        .username("testuser")
                        .email("test@example.com")
                        .password("secret")
                        .build();

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        UserPrincipal principal = UserPrincipal.from(user);
        context.setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        principal, null, principal.getAuthorities()));
        SecurityContextHolder.setContext(context);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        cartCaptor = ArgumentCaptor.forClass(Cart.class);
        when(cartMapper.toResponse(cartCaptor.capture()))
                .thenReturn(CartResponse.builder().build());

        when(cartRepository.save(any(Cart.class)))
                .thenAnswer(
                        invocation -> {
                            Cart cart = invocation.getArgument(0, Cart.class);
                            if (cart.getId() == null) {
                                cart.setId(UUID.randomUUID());
                            }
                            if (cart.getItems() == null) {
                                cart.setItems(new ArrayList<>());
                            }
                            return cart;
                        });
        when(cartItemRepository.findByCartIdAndProductVariantId(any(), any()))
                .thenReturn(Optional.empty());
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("addItem")
    class AddItemTests {

        @Test
        @DisplayName("Should add new item to empty cart")
        void shouldAddNewItemToEmptyCart() {
            UUID variantId = UUID.randomUUID();
            BigDecimal price = new BigDecimal("150000.00");

            Product product =
                    Product.builder().id(UUID.randomUUID()).name("Perfume A").active(true).build();
            ProductVariant variant =
                    ProductVariant.builder()
                            .id(variantId)
                            .product(product)
                            .variantSku("SKU-123")
                            .volumeMl(new BigDecimal("50.00"))
                            .packageType("Bottle")
                            .price(price)
                            .stockQuantity(10)
                            .active(true)
                            .build();

            when(cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE))
                    .thenReturn(Optional.empty());
            when(productVariantRepository.findById(variantId)).thenReturn(Optional.of(variant));

            CartItemCreateRequest request =
                    CartItemCreateRequest.builder().variantId(variantId).quantity(2).build();

            cartItemService.addItem(request);

            Cart capturedCart = cartCaptor.getValue();
            assertThat(capturedCart.getItems()).hasSize(1);

            CartItem capturedItem = capturedCart.getItems().get(0);
            assertThat(capturedItem.getQuantity()).isEqualTo(2);
            assertThat(capturedCart.getTotalItems()).isEqualTo(2);
            assertThat(capturedCart.getSubtotalAmount()).isEqualByComparingTo("300000.00");
            assertThat(capturedCart.getTotalAmount()).isEqualByComparingTo("300000.00");

            verify(cartRepository).findByUserIdAndStatus(userId, CartStatus.ACTIVE);
            verify(productVariantRepository).findById(variantId);
        }

        @Test
        @DisplayName("Should increase quantity when item already exists in cart")
        void shouldIncreaseQuantityWhenItemAlreadyExists() {
            UUID cartId = UUID.randomUUID();
            UUID variantId = UUID.randomUUID();
            BigDecimal price = new BigDecimal("99000.00");

            Product product =
                    Product.builder().id(UUID.randomUUID()).name("Perfume B").active(true).build();
            ProductVariant variant =
                    ProductVariant.builder()
                            .id(variantId)
                            .product(product)
                            .variantSku("SKU-456")
                            .volumeMl(new BigDecimal("100.00"))
                            .packageType("Bottle")
                            .price(price)
                            .stockQuantity(20)
                            .active(true)
                            .build();

            Cart existingCart =
                    Cart.builder().id(cartId).user(user).status(CartStatus.ACTIVE).build();

            CartItem existingItem =
                    CartItem.builder()
                            .id(UUID.randomUUID())
                            .cart(existingCart)
                            .product(product)
                            .productVariant(variant)
                            .quantity(1)
                            .price(price)
                            .subTotalAmount(price)
                            .build();
            existingCart.getItems().add(existingItem);

            when(cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE))
                    .thenReturn(Optional.of(existingCart));
            when(cartItemRepository.findByCartIdAndProductVariantId(cartId, variantId))
                    .thenReturn(Optional.of(existingItem));
            when(productVariantRepository.findById(variantId)).thenReturn(Optional.of(variant));

            CartItemCreateRequest request =
                    CartItemCreateRequest.builder().variantId(variantId).quantity(3).build();

            cartItemService.addItem(request);

            Cart capturedCart = cartCaptor.getValue();
            CartItem capturedItem = capturedCart.getItems().get(0);
            assertThat(capturedItem.getQuantity()).isEqualTo(4);
            assertThat(capturedCart.getTotalItems()).isEqualTo(4);
            assertThat(capturedCart.getSubtotalAmount()).isEqualByComparingTo("396000.00");
        }
    }

    @Nested
    @DisplayName("updateItem & removeItem")
    class UpdateAndRemoveTests {

        @Test
        @DisplayName("Should update quantity for existing cart item")
        void shouldUpdateCartItemQuantity() {
            UUID cartId = UUID.randomUUID();
            UUID itemId = UUID.randomUUID();
            UUID variantId = UUID.randomUUID();
            BigDecimal price = new BigDecimal("120000.00");

            Product product =
                    Product.builder().id(UUID.randomUUID()).name("Perfume C").active(true).build();
            ProductVariant variant =
                    ProductVariant.builder()
                            .id(variantId)
                            .product(product)
                            .variantSku("SKU-789")
                            .volumeMl(new BigDecimal("75.00"))
                            .packageType("Bottle")
                            .price(price)
                            .stockQuantity(15)
                            .active(true)
                            .build();

            Cart cart = Cart.builder().id(cartId).user(user).status(CartStatus.ACTIVE).build();

            CartItem item =
                    CartItem.builder()
                            .id(itemId)
                            .cart(cart)
                            .product(product)
                            .productVariant(variant)
                            .quantity(2)
                            .price(price)
                            .subTotalAmount(price.multiply(BigDecimal.valueOf(2)))
                            .build();
            cart.getItems().add(item);

            when(cartItemRepository.findById(itemId)).thenReturn(Optional.of(item));
            when(productVariantRepository.findById(variantId)).thenReturn(Optional.of(variant));

            CartItemUpdateRequest request = CartItemUpdateRequest.builder().quantity(5).build();

            cartItemService.updateItem(itemId, request);

            Cart capturedCart = cartCaptor.getValue();
            CartItem capturedItem = capturedCart.getItems().get(0);
            assertThat(capturedItem.getQuantity()).isEqualTo(5);
            assertThat(capturedCart.getSubtotalAmount()).isEqualByComparingTo("600000.00");
        }

        @Test
        @DisplayName("Should remove cart item and recalculate totals")
        void shouldRemoveCartItem() {
            UUID cartId = UUID.randomUUID();
            UUID itemId = UUID.randomUUID();
            BigDecimal price = new BigDecimal("50000.00");

            Product product =
                    Product.builder().id(UUID.randomUUID()).name("Perfume D").active(true).build();
            ProductVariant variant =
                    ProductVariant.builder()
                            .id(UUID.randomUUID())
                            .product(product)
                            .variantSku("SKU-000")
                            .price(price)
                            .stockQuantity(5)
                            .active(true)
                            .build();

            Cart cart = Cart.builder().id(cartId).user(user).status(CartStatus.ACTIVE).build();
            CartItem item =
                    CartItem.builder()
                            .id(itemId)
                            .cart(cart)
                            .product(product)
                            .productVariant(variant)
                            .quantity(2)
                            .price(price)
                            .subTotalAmount(price.multiply(BigDecimal.valueOf(2)))
                            .build();
            cart.getItems().add(item);
            cart.setTotalItems(2);
            cart.setSubtotalAmount(price.multiply(BigDecimal.valueOf(2)));
            cart.setTotalAmount(price.multiply(BigDecimal.valueOf(2)));

            when(cartItemRepository.findById(itemId)).thenReturn(Optional.of(item));

            cartItemService.removeItem(itemId);

            Cart capturedCart = cartCaptor.getValue();
            assertThat(capturedCart.getItems()).isEmpty();
            assertThat(capturedCart.getTotalItems()).isZero();
            assertThat(capturedCart.getSubtotalAmount()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(capturedCart.getTotalAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    @Test
    @DisplayName("Should clear cart and reset totals")
    void shouldClearCart() {
        UUID cartId = UUID.randomUUID();
        Cart cart =
                Cart.builder()
                        .id(cartId)
                        .user(user)
                        .status(CartStatus.ACTIVE)
                        .totalItems(2)
                        .subtotalAmount(new BigDecimal("100000.00"))
                        .discountAmount(BigDecimal.ZERO)
                        .totalAmount(new BigDecimal("100000.00"))
                        .build();
        cart.setItems(new ArrayList<>());

        when(cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE))
                .thenReturn(Optional.of(cart));

        cartItemService.clearCart();

        Cart capturedCart = cartCaptor.getValue();
        assertThat(capturedCart.getItems()).isEmpty();
        assertThat(capturedCart.getTotalItems()).isZero();
        assertThat(capturedCart.getSubtotalAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(capturedCart.getTotalAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Should throw when authentication missing")
    void shouldThrowWhenUnauthorized() {
        SecurityContextHolder.clearContext();

        CartItemCreateRequest request =
                CartItemCreateRequest.builder().variantId(UUID.randomUUID()).quantity(1).build();

        assertThatThrownBy(() -> cartItemService.addItem(request))
                .isInstanceOf(ApiException.class)
                .extracting("errorCode")
                .hasToString("UNAUTHORIZED");
    }
}
