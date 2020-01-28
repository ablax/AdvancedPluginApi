package me.ablax.decode.caching;

import java.util.List;

interface GuavaCache<KEY, VALUE> {

    VALUE get(KEY key);

    List<VALUE> getAll(List<KEY> keys);

    void put(KEY key, VALUE value);

}
