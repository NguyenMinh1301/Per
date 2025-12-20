package com.per.common.cache;

/**
 * Centralized cache name constants. Single source of truth for all cache names in the application
 * (DRY principle).
 */
public final class CacheNames {

    private CacheNames() {
        // Prevent instantiation
    }

    // Product caches
    public static final String PRODUCTS = "products";
    public static final String PRODUCT = "product";

    // Master data caches
    public static final String CATEGORIES = "categories";
    public static final String CATEGORY = "category";

    public static final String BRANDS = "brands";
    public static final String BRAND = "brand";

    public static final String MADE_INS = "madeIns";
    public static final String MADE_IN = "madeIn";
}
