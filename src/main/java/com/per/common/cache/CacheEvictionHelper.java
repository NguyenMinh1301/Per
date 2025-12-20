package com.per.common.cache;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import lombok.RequiredArgsConstructor;

/**
 * Helper class for cache eviction with proper transaction handling.
 *
 * <p>This class ensures cache eviction happens AFTER database transaction commits, preventing race
 * conditions where stale data could be cached.
 *
 * <p>Problem with naive eviction:
 *
 * <pre>
 * Thread A: Evict cache → [Thread B reads old DB → caches old value] → Commit DB
 * Result: Cache has stale data!
 * </pre>
 *
 * <p>Solution: Post-commit eviction
 *
 * <pre>
 * Thread A: Commit DB → Evict cache
 * Result: Cache always reflects latest DB state
 * </pre>
 */
@Component
@RequiredArgsConstructor
public class CacheEvictionHelper {

    private static final Logger log = LoggerFactory.getLogger(CacheEvictionHelper.class);

    private final CacheManager cacheManager;

    /**
     * Evicts a specific key from cache AFTER the current transaction commits. If no transaction is
     * active, evicts immediately.
     *
     * @param cacheName the name of the cache
     * @param key the key to evict
     */
    public void evictAfterCommit(String cacheName, Object key) {
        Objects.requireNonNull(cacheName, "cacheName must not be null");
        Objects.requireNonNull(key, "key must not be null");

        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            doEvict(cacheName, key);
                        }
                    });
        } else {
            doEvict(cacheName, key);
        }
    }

    /**
     * Evicts all entries from the specified cache AFTER the current transaction commits. If no
     * transaction is active, evicts immediately.
     *
     * @param cacheName the name of the cache to clear
     */
    public void evictAllAfterCommit(String cacheName) {
        Objects.requireNonNull(cacheName, "cacheName must not be null");

        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            doClear(cacheName);
                        }
                    });
        } else {
            doClear(cacheName);
        }
    }

    /**
     * Evicts all entries from multiple caches AFTER the current transaction commits. Useful when an
     * update affects multiple cache views (e.g., list and detail caches).
     *
     * @param cacheNames the names of the caches to clear
     */
    public void evictAllAfterCommit(String... cacheNames) {
        Objects.requireNonNull(cacheNames, "cacheNames must not be null");

        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            for (String cacheName : cacheNames) {
                                doClear(cacheName);
                            }
                        }
                    });
        } else {
            for (String cacheName : cacheNames) {
                doClear(cacheName);
            }
        }
    }

    private void doEvict(String cacheName, Object key) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.evict(key);
            log.debug("Evicted key '{}' from cache '{}'", key, cacheName);
        }
    }

    private void doClear(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
            log.debug("Cleared cache '{}'", cacheName);
        }
    }
}
