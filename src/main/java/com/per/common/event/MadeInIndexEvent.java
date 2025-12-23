package com.per.common.event;

import com.per.made_in.document.MadeInDocument;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Kafka event for syncing made-in data to Elasticsearch. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MadeInIndexEvent {

    public enum Action {
        INDEX,
        DELETE
    }

    private Action action;
    private String madeInId;
    private MadeInDocument document;
}
