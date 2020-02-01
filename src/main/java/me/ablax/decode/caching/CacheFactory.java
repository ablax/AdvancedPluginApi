package me.ablax.decode.caching;

import java.util.concurrent.TimeUnit;

public class CacheFactory {

    private CacheFactory() {

    }

    public static <K, V> OptionalCache<K, V> buildOptionalCache(long maxSize, long expireAfter, TimeUnit expireAfterUnit) {
        return new OptionalCacheImpl<>(maxSize, expireAfter, expireAfterUnit);
    }

    static <K, V> GuavaCache<K, V> buildGuavaCache() {
        return new GuavaCacheImpl<>();
    }

}
