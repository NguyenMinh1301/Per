package com.per.cart.helper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.per.auth.repository.UserRepository;
import com.per.auth.security.principal.UserPrincipal;
import com.per.cart.entity.Cart;
import com.per.cart.enums.CartStatus;
import com.per.cart.repository.CartRepository;
import com.per.common.exception.ApiErrorCode;
import com.per.common.exception.ApiException;
import com.per.user.entity.User;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CartHelper {

    private final CartRepository cartRepository;
    private final UserRepository userRepository;

    public User requireCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ApiException(ApiErrorCode.UNAUTHORIZED, "Unauthorized");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserPrincipal userPrincipal) {
            UUID userId = userPrincipal.getUser().getId();
            return userRepository
                    .findById(userId)
                    .orElseThrow(() -> new ApiException(ApiErrorCode.NOT_FOUND, "User not found"));
        }
        String username = authentication.getName();
        return userRepository
                .findByUsername(username)
                .orElseThrow(() -> new ApiException(ApiErrorCode.NOT_FOUND, "User not found"));
    }

    public Cart getOrCreateActiveCart(User user) {
        Optional<Cart> existing =
                cartRepository.findByUserIdAndStatus(user.getId(), CartStatus.ACTIVE);
        if (existing.isPresent()) {
            return existing.get();
        }
        Cart cart = Cart.builder().user(user).status(CartStatus.ACTIVE).build();
        return cartRepository.save(cart);
    }

    public void recalculateCart(Cart cart) {
        int totalItems = cart.getItems().stream().mapToInt(item -> item.getQuantity()).sum();
        BigDecimal subtotal =
                cart.getItems().stream()
                        .map(item -> item.getSubTotalAmount())
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        .setScale(2, RoundingMode.HALF_UP);

        BigDecimal discount =
                cart.getDiscountAmount() == null ? BigDecimal.ZERO : cart.getDiscountAmount();
        BigDecimal total = subtotal.subtract(discount);
        if (total.compareTo(BigDecimal.ZERO) < 0) {
            total = BigDecimal.ZERO;
        }

        cart.setTotalItems(totalItems);
        cart.setSubtotalAmount(subtotal);
        cart.setDiscountAmount(discount);
        cart.setTotalAmount(total.setScale(2, RoundingMode.HALF_UP));
    }
}
