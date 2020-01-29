package me.ablax.decode.caching;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

class GuavaCacheImpl<GUAVAKEY, GUAVAVALUE> implements GuavaCache<GUAVAKEY, GUAVAVALUE> {

    private LoadingCache<GUAVAKEY, GUAVAVALUE> cache;

    @Override
    public void init(Function<GUAVAKEY, GUAVAVALUE> loadFunction,
                     long maxSize,
                     long expireAfter, TimeUnit expireAfterUnit) {

        CacheLoader<GUAVAKEY, GUAVAVALUE> cacheLoader = new CacheLoader<GUAVAKEY, GUAVAVALUE>() {
            @Override
            public GUAVAVALUE load(GUAVAKEY key) {
                return loadFunction.apply(key);
            }

        };

        cache = CacheBuilder.newBuilder().recordStats()
                .maximumSize(maxSize)
                .expireAfterAccess(expireAfter, expireAfterUnit)
                .build(cacheLoader);
    }

    @Override
    public void init(Function<GUAVAKEY, GUAVAVALUE> loadFunction,
                     Function<Iterable<? extends GUAVAKEY>, Map<GUAVAKEY, GUAVAVALUE>> loadAllFunction,
                     long maxSize,
                     long expireAfter, TimeUnit expireAfterUnit) {

        CacheLoader<GUAVAKEY, GUAVAVALUE> cacheLoader = new CacheLoader<GUAVAKEY, GUAVAVALUE>() {
            @Override
            public GUAVAVALUE load(GUAVAKEY key) {
                return loadFunction.apply(key);
            }

            @Override
            public Map<GUAVAKEY, GUAVAVALUE> loadAll(Iterable<? extends GUAVAKEY> pendingList) {
                return loadAllFunction.apply(pendingList);
            }
        };

        cache = CacheBuilder.newBuilder().recordStats()
                .maximumSize(maxSize)
                .expireAfterAccess(expireAfter,
                        expireAfterUnit)
                .build(cacheLoader);
    }

    @Override
    public GUAVAVALUE getByKey(GUAVAKEY key) {
        return cache.getIfPresent(key);
    }

    @Override
    public GUAVAVALUE loadAndGetByKey(GUAVAKEY key) throws ExecutionException {
        return cache.get(key);
    }

    @Override
    public ImmutableMap<GUAVAKEY, GUAVAVALUE> getAll(List<GUAVAKEY> pendingList) {
        return cache.getAllPresent(pendingList);
    }

    @Override
    public ImmutableMap<GUAVAKEY, GUAVAVALUE> loadAndGetAll(List<GUAVAKEY> pendingList) throws ExecutionException {
        return cache.getAll(pendingList);
    }

    @Override
    public boolean isPresent(GUAVAKEY key) {
        return cache.asMap().containsKey(key);
    }

    @Override
    public void put(GUAVAKEY key, GUAVAVALUE value) {
        cache.put(key, value);
    }

    @Override
    public long size() {
        return cache.size();
    }

    @Override
    public void putAll(Map<? extends GUAVAKEY, ? extends GUAVAVALUE> map) {
        cache.putAll(map);
    }

    @Override
    public void invalidate(GUAVAKEY key) {
        cache.invalidate(key);
    }

    @Override
    public String getStats() {
        return cache.stats().toString();
    }
}
