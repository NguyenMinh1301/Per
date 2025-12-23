package com.per.common.event;

import com.per.category.document.CategoryDocument;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Kafka event for syncing category data to Elasticsearch. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryIndexEvent {

    public enum Action {
        INDEX,
        DELETE
    }

    private Action action;
    private String categoryId;
    private CategoryDocument document;
}
