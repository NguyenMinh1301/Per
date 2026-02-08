package com.per.common.event.cdc;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;

/** CDC payload for Brand entity from Debezium. */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BrandCdcPayload implements CdcPayload {

    private String id;
    private String name;
    private String description;

    @JsonProperty("logo_url")
    private String logoUrl;

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
