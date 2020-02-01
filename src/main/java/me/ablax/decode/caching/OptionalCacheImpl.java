package me.ablax.decode.caching;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

class OptionalCacheImpl<LASTROWKEY, LASTROWENT> implements OptionalCache<LASTROWKEY, LASTROWENT> {

    private final long maxSize;
    private final long expireAfter;
    private final TimeUnit expireAfterUnit;

    private GuavaCache<LASTROWKEY, Optional<LASTROWENT>> guavaCache = CacheFactory.buildGuavaCache();

    public OptionalCacheImpl(long maxSize, long expireAfter, TimeUnit expireAfterUnit) {
        this.maxSize = maxSize;
        this.expireAfter = expireAfter;
        this.expireAfterUnit = expireAfterUnit;
    }

    @Override
    public void init(Function<LASTROWKEY, Optional<LASTROWENT>> loadFunction) {
        guavaCache.init(loadFunction, maxSize, expireAfter, expireAfterUnit);
    }

    @Override
    public void init(Function<LASTROWKEY, Optional<LASTROWENT>> loadFunction, Function<Iterable<? extends LASTROWKEY>, Map<LASTROWKEY, Optional<LASTROWENT>>> loadAllFunction) {
        guavaCache.init(loadFunction, loadAllFunction, maxSize, expireAfter, expireAfterUnit);
    }

    @Override
    public LASTROWENT getByKey(LASTROWKEY key) {
        Optional<LASTROWENT> transferHistory = guavaCache.getByKey(key);
        return transferHistory == null ? null : transferHistory.orElse(null);
    }

    @Override
    public LASTROWENT loadAndGetByKey(LASTROWKEY key) throws ExecutionException {
        Optional<LASTROWENT> transferHistory;
        try {
            transferHistory = guavaCache.loadAndGetByKey(key);
        } catch (ExecutionException e) {
            throw new ExecutionException(e);
        }
        return transferHistory.orElse(null);
    }

    @Override
    public Map<LASTROWKEY, LASTROWENT> getAll(List<LASTROWKEY> pendingList) {
        return guavaCache.getAll(pendingList).entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().get()));
    }

    @Override
    public Map<LASTROWKEY, LASTROWENT> loadAndGetAll(List<LASTROWKEY> pendingList) throws ExecutionException {
        return guavaCache.loadAndGetAll(pendingList).entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().get()));
    }

    @Override
    public boolean isPresent(LASTROWKEY key) {
        return guavaCache.isPresent(key);
    }

    @Override
    public void put(LASTROWKEY key, LASTROWENT value) {
        guavaCache.put(key, Optional.ofNullable(value));
    }

    @Override
    public void putAll(Map<? extends LASTROWKEY, ? extends LASTROWENT> map) {
        guavaCache.putAll(map.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> Optional.ofNullable(e.getValue()))));
    }

    @Override
    public long size() {
        return guavaCache.size();
    }


    @Override
    public String getStats() {
        return guavaCache.getStats();
    }

}
