package me.ablax.decode.caching;

import java.util.concurrent.TimeUnit;

public class CacheFactory {

    private CacheFactory() {

    }

    public static OptionalCache buildOptionalCache(long maxSize, long expireAfter, TimeUnit expireAfterUnit) {
        return new OptionalCacheImpl<>(maxSize, expireAfter, expireAfterUnit);
    }

    static GuavaCache buildGuavaCache() {
        return new GuavaCacheImpl<>();
    }

}
