package me.ablax.decode.caching;

import com.google.common.collect.ImmutableMap;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

interface GuavaCache<GUAVAKEY, GUAVAVALUE> {

    void init(Function<GUAVAKEY, GUAVAVALUE> loadFunction,
              long maxSize,
              long expireAfter, TimeUnit expireAfterUnit);

    void init(Function<GUAVAKEY, GUAVAVALUE> loadFunction,
              Function<Iterable<? extends GUAVAKEY>, Map<GUAVAKEY, GUAVAVALUE>> loadAllFunction,
              long maxSize,
              long expireAfter, TimeUnit expireAfterUnit);

    GUAVAVALUE getByKey(GUAVAKEY key);

    GUAVAVALUE loadAndGetByKey(GUAVAKEY key) throws ExecutionException;

    ImmutableMap<GUAVAKEY, GUAVAVALUE> getAll(List<GUAVAKEY> pendingList);

    ImmutableMap<GUAVAKEY, GUAVAVALUE> loadAndGetAll(List<GUAVAKEY> pendingList) throws ExecutionException;

    boolean isPresent(GUAVAKEY key);

    void put(GUAVAKEY key, GUAVAVALUE guavavalue);

    long size();

    void putAll(Map<? extends GUAVAKEY, ? extends GUAVAVALUE> map);

    void invalidate(GUAVAKEY key);

    String getStats();

}
