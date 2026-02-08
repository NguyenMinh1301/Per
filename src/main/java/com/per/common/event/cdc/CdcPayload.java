package com.per.common.event.cdc;

/** Marker interface for CDC payload DTOs. All CDC entity payloads should implement this. */
public interface CdcPayload {

    /** Returns the entity ID (UUID as String). */
    String getId();

    /**
     * Returns the CDC operation type (__op field from SMT). c=create, u=update, d=delete,
     * r=read(snapshot)
     */
    String getOp();

    /** Returns the __deleted flag from SMT rewrite mode. */
    Boolean getDeleted();

    /** Check if this is a delete operation. */
    default boolean isDeleted() {
        return "d".equals(getOp()) || Boolean.TRUE.equals(getDeleted());
    }

    /** Check if this is a create operation (including snapshot reads). */
    default boolean isCreate() {
        return "c".equals(getOp()) || "r".equals(getOp());
    }

    /** Check if this is an update operation. */
    default boolean isUpdate() {
        return "u".equals(getOp());
    }
}
