package com.per.cart.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.per.cart.dto.response.CartResponse;
import com.per.cart.entity.Cart;
import com.per.cart.helper.CartHelper;
import com.per.cart.mapper.CartMapper;
import com.per.cart.repository.CartRepository;
import com.per.cart.service.CartService;
import com.per.user.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartMapper cartMapper;
    private final CartHelper cartHelper;

    @Override
    @Transactional(readOnly = true)
    public CartResponse getActiveCart() {
        User user = cartHelper.requireCurrentUser();
        Cart cart = cartHelper.getOrCreateActiveCart(user);
        return cartMapper.toResponse(cart);
    }
}
