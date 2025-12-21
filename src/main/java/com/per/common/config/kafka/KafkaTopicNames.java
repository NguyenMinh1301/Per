package com.per.common.config.kafka;

/**
 * Centralized Kafka topic name constants. All topics should be defined here to ensure consistency
 * and enable easy extension.
 */
public final class KafkaTopicNames {

    // Email topics
    public static final String EMAIL_TOPIC = "email-topic";
    public static final String EMAIL_GROUP = "email-group";

    // Add future topics here:
    // public static final String ORDER_TOPIC = "order-topic";
    // public static final String ORDER_GROUP = "order-group";
    // public static final String NOTIFICATION_TOPIC = "notification-topic";
    // public static final String NOTIFICATION_GROUP = "notification-group";

    private KafkaTopicNames() {
        // Utility class - prevent instantiation
    }
}
