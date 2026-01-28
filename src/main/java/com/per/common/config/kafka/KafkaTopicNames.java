package com.per.common.config.kafka;

/**
 * Centralized Kafka topic name constants. All topics should be defined here to ensure consistency
 * and enable easy extension.
 */
public final class KafkaTopicNames {

    // Email topics
    public static final String EMAIL_SEND_TOPIC = "notification.email.send";
    public static final String EMAIL_DLQ_TOPIC = "notification.email.dlq";
    public static final String EMAIL_GROUP = "email-group";

    // Product index topics (Elasticsearch sync)
    public static final String PRODUCT_INDEX_TOPIC = "product-index-topic";
    public static final String PRODUCT_INDEX_GROUP = "product-index-group";

    // Brand index topics (Elasticsearch sync)
    public static final String BRAND_INDEX_TOPIC = "brand-index-topic";
    public static final String BRAND_INDEX_GROUP = "brand-index-group";

    // Category index topics (Elasticsearch sync)
    public static final String CATEGORY_INDEX_TOPIC = "category-index-topic";
    public static final String CATEGORY_INDEX_GROUP = "category-index-group";

    // MadeIn index topics (Elasticsearch sync)
    public static final String MADEIN_INDEX_TOPIC = "madein-index-topic";
    public static final String MADEIN_INDEX_GROUP = "madein-index-group";

    private KafkaTopicNames() {
        // Utility class - prevent instantiation
    }
}
