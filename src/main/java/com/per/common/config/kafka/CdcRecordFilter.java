package com.per.common.config.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.listener.adapter.RecordFilterStrategy;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Filters out Kafka tombstone records (null values) to prevent NPE. Tombstones are emitted by
 * Debezium after DELETE for log compaction.
 */
@Component
@Slf4j
public class CdcRecordFilter implements RecordFilterStrategy<String, Object> {

    @Override
    public boolean filter(ConsumerRecord<String, Object> record) {
        // Return true to SKIP/filter out the record
        if (record.value() == null) {
            log.debug("Filtering tombstone record for key: {}", record.key());
            return true;
        }
        return false;
    }
}
