package com.per.common.config.kafka;

/**
 * Centralized Kafka topic name constants. All topics should be defined here to ensure consistency
 * and enable easy extension.
 */
public final class KafkaTopicNames {

    // Email topics
    public static final String EMAIL_TOPIC = "email-topic";
    public static final String EMAIL_GROUP = "email-group";

    // Product index topics (Elasticsearch sync)
    public static final String PRODUCT_INDEX_TOPIC = "product-index-topic";
    public static final String PRODUCT_INDEX_GROUP = "product-index-group";

    private KafkaTopicNames() {
        // Utility class - prevent instantiation
    }
}
