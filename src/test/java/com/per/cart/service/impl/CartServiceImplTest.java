package com.per.cart.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.per.cart.dto.response.CartResponse;
import com.per.cart.entity.Cart;
import com.per.cart.helper.CartHelper;
import com.per.cart.mapper.CartMapper;
import com.per.cart.repository.CartRepository;
import com.per.user.entity.User;

@ExtendWith(MockitoExtension.class)
@DisplayName("CartServiceImpl Tests")
class CartServiceImplTest {

    @Mock private CartRepository cartRepository;
    @Mock private CartMapper cartMapper;
    @Mock private CartHelper cartHelper;

    @InjectMocks private CartServiceImpl cartService;

    private User user;
    private Cart cart;

    @BeforeEach
    void setUp() {
        user = User.builder().id(UUID.randomUUID()).username("u").email("u@test.com").build();
        cart = Cart.builder().id(UUID.randomUUID()).user(user).build();
    }

    @Test
    @DisplayName("Should return active cart for current user")
    void shouldReturnActiveCart() {
        CartResponse expected =
                CartResponse.builder().id(cart.getId()).userId(user.getId()).build();

        when(cartHelper.requireCurrentUser()).thenReturn(user);
        when(cartHelper.getOrCreateActiveCart(user)).thenReturn(cart);
        when(cartMapper.toResponse(cart)).thenReturn(expected);

        CartResponse response = cartService.getActiveCart();

        assertThat(response).isEqualTo(expected);
        verify(cartHelper).requireCurrentUser();
        verify(cartHelper).getOrCreateActiveCart(user);
        verify(cartMapper).toResponse(any(Cart.class));
    }
}
