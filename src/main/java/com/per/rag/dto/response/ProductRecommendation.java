package com.per.rag.dto.response;

import java.math.BigDecimal;

public record ProductRecommendation(
        String id, String name, BigDecimal price, String reasonForRecommendation) {}
