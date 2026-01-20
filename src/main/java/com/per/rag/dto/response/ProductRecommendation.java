package com.per.rag.dto.response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRecommendation {
    private String id;
    private String name;
    private BigDecimal price;
    private String reasonForRecommendation;
}
