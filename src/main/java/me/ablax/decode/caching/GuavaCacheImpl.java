package me.ablax.decode.caching;

import java.util.List;
import java.util.Optional;

class GuavaCacheImpl<KEY, VALUE> implements GuavaCache<KEY, Optional<VALUE>> {

    @Override
    public List getAll(List list) {
        return null;
    }

    @Override
    public Optional<VALUE> get(KEY key) {
        return null;
    }

    @Override
    public void put(KEY key, Optional<VALUE> valueOptional) {

    }
}
