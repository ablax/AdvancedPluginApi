package me.ablax.decode.caching;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

public interface OptionalCache<DATAKEY, DATA> {

    void init(Function<DATAKEY, Optional<DATA>> loadFunction);

    void init(Function<DATAKEY, Optional<DATA>> loadFunction, Function<Iterable<? extends DATAKEY>, Map<DATAKEY, Optional<DATA>>> loadAllFunction);

    DATA getByKey(DATAKEY key);

    DATA loadAndGetByKey(DATAKEY key) throws ExecutionException;

    Map<DATAKEY, DATA> getAll(List<DATAKEY> pendingList);

    Map<DATAKEY, DATA> loadAndGetAll(List<DATAKEY> pendingList) throws ExecutionException;

    boolean isPresent(DATAKEY key);

    void put(DATAKEY key, DATA value);

    void putAll(Map<? extends DATAKEY, ? extends DATA> map);

    long size();

    String getStats();

}
