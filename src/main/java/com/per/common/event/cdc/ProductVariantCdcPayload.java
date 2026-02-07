package com.per.common.event.cdc;

import java.math.BigDecimal;
import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * CDC payload for ProductVariant entity from Debezium. Used to trigger parent Product re-indexing
 * when variant changes.
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductVariantCdcPayload implements CdcPayload {

    private String id;

    @JsonProperty("product_id")
    private String productId;

    @JsonProperty("variant_sku")
    private String variantSku;

    @JsonProperty("variant_name")
    private String variantName;

    private BigDecimal price;

    @JsonProperty("stock_quantity")
    private Integer stockQuantity;

    @JsonProperty("low_stock_threshold")
    private Integer lowStockThreshold;

    @JsonProperty("currency_code")
    private String currencyCode;

    @JsonProperty("created_at")
    private Instant createdAt;

    @JsonProperty("updated_at")
    private Instant updatedAt;

    // CDC Metadata fields
    @JsonProperty("__op")
    private String op;

    @JsonProperty("__source_ts_ms")
    private Long sourceTsMs;

    @JsonProperty("__table")
    private String table;

    @JsonProperty("__deleted")
    private Boolean deleted;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getOp() {
        return op;
    }

    @Override
    public Boolean getDeleted() {
        return deleted;
    }
}
