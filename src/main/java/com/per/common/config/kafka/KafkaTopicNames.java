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

    // Legacy index topics (to be removed after CDC migration)
    @Deprecated public static final String PRODUCT_INDEX_TOPIC = "product-index-topic";
    @Deprecated public static final String PRODUCT_INDEX_GROUP = "product-index-group";
    @Deprecated public static final String BRAND_INDEX_TOPIC = "brand-index-topic";
    @Deprecated public static final String BRAND_INDEX_GROUP = "brand-index-group";
    @Deprecated public static final String CATEGORY_INDEX_TOPIC = "category-index-topic";
    @Deprecated public static final String CATEGORY_INDEX_GROUP = "category-index-group";
    @Deprecated public static final String MADEIN_INDEX_TOPIC = "madein-index-topic";
    @Deprecated public static final String MADEIN_INDEX_GROUP = "madein-index-group";

    // ========== Debezium CDC Topics ==========
    // Format: {topic.prefix}.{schema}.{table}

    public static final String CDC_PRODUCTS_TOPIC = "per.public.product";
    public static final String CDC_PRODUCT_VARIANTS_TOPIC = "per.public.product_variant";
    public static final String CDC_BRANDS_TOPIC = "per.public.brand";
    public static final String CDC_CATEGORIES_TOPIC = "per.public.category";
    public static final String CDC_MADE_INS_TOPIC = "per.public.made_id";

    // CDC Consumer Groups
    public static final String CDC_PRODUCT_GROUP = "product-cdc-group";
    public static final String CDC_PRODUCT_VARIANT_GROUP = "product-variant-cdc-group";
    public static final String CDC_BRAND_GROUP = "brand-cdc-group";
    public static final String CDC_CATEGORY_GROUP = "category-cdc-group";
    public static final String CDC_MADEIN_GROUP = "madein-cdc-group";

    private KafkaTopicNames() {
        // Utility class - prevent instantiation
    }
}
