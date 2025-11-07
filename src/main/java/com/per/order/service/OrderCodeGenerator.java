package com.per.order.service;

import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Component;

import com.per.order.repository.OrderRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OrderCodeGenerator {

    private static final long MIN_ORDER_CODE = 100_000_000L;
    private static final long MAX_ORDER_CODE = 999_999_999L;

    private final OrderRepository orderRepository;

    public long nextOrderCode() {
        for (int attempt = 0; attempt < 5; attempt++) {
            long candidate =
                    ThreadLocalRandom.current().nextLong(MIN_ORDER_CODE, MAX_ORDER_CODE + 1);
            if (!orderRepository.existsByOrderCode(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException("Unable to allocate unique order code");
    }
}
