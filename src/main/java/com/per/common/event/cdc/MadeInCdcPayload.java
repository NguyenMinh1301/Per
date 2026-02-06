package com.per.common.event.cdc;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;

/** CDC payload for MadeIn entity from Debezium. */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MadeInCdcPayload implements CdcPayload {

    private String id;
    private String name;
    private String description;

    @JsonProperty("is_active")
    private Boolean isActive;

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
