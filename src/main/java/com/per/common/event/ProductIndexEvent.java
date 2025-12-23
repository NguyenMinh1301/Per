package com.per.common.event;

import com.per.product.document.ProductDocument;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Kafka event for syncing product data to Elasticsearch. Published by ProductServiceImpl on
 * create/update/delete operations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductIndexEvent {

    /** Action type for the indexing operation. */
    public enum Action {
        /** Index or update a product document. */
        INDEX,
        /** Delete a product document from the index. */
        DELETE
    }

    private Action action;
    private String productId;
    private ProductDocument document; // null for DELETE action
}
