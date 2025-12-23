package com.per.common.event;

import com.per.brand.document.BrandDocument;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Kafka event for syncing brand data to Elasticsearch. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrandIndexEvent {

    public enum Action {
        INDEX,
        DELETE
    }

    private Action action;
    private String brandId;
    private BrandDocument document;
}
