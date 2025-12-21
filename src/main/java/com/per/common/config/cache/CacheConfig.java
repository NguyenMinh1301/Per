package com.per.common.config.cache;

import java.time.Duration;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Redis cache configuration with customized TTL per cache type.
 *
 * <p>TTL Strategy:
 *
 * <ul>
 *   <li>Product caches: 10 minutes (higher traffic, more frequent updates)
 *   <li>Master data caches: 30 minutes (low update frequency)
 * </ul>
 */
@Configuration
@EnableCaching
public class CacheConfig {

    private static final Duration DEFAULT_TTL = Duration.ofMinutes(15);
    private static final Duration PRODUCT_TTL = Duration.ofMinutes(10);
    private static final Duration MASTER_DATA_TTL = Duration.ofMinutes(30);

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration defaultConfig = createCacheConfig(DEFAULT_TTL);

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withCacheConfiguration(CacheNames.PRODUCTS, createCacheConfig(PRODUCT_TTL))
                .withCacheConfiguration(CacheNames.PRODUCT, createCacheConfig(PRODUCT_TTL))
                .withCacheConfiguration(CacheNames.CATEGORIES, createCacheConfig(MASTER_DATA_TTL))
                .withCacheConfiguration(CacheNames.CATEGORY, createCacheConfig(MASTER_DATA_TTL))
                .withCacheConfiguration(CacheNames.BRANDS, createCacheConfig(MASTER_DATA_TTL))
                .withCacheConfiguration(CacheNames.BRAND, createCacheConfig(MASTER_DATA_TTL))
                .withCacheConfiguration(CacheNames.MADE_INS, createCacheConfig(MASTER_DATA_TTL))
                .withCacheConfiguration(CacheNames.MADE_IN, createCacheConfig(MASTER_DATA_TTL))
                .transactionAware()
                .build();
    }

    private RedisCacheConfiguration createCacheConfig(Duration ttl) {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(ttl)
                .disableCachingNullValues()
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new StringRedisSerializer()))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new GenericJackson2JsonRedisSerializer(redisObjectMapper())));
    }

    /** Configures ObjectMapper with Java 8 date/time support for Redis serialization. */
    private ObjectMapper redisObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.activateDefaultTyping(
                mapper.getPolymorphicTypeValidator(), ObjectMapper.DefaultTyping.NON_FINAL);
        return mapper;
    }
}
