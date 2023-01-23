package fi.nls.hakunapi.simple.servlet;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import com.github.benmanes.caffeine.cache.LoadingCache;

public class CacheManager {
    
    private final ConcurrentHashMap<String, LoadingCache> caches = new ConcurrentHashMap<>();

    public <K, V> LoadingCache<K, V> getCache(String key, Supplier<LoadingCache<? super K, ? super V>> supplier) {
        return caches.computeIfAbsent(key, __ -> supplier.get());
    }

}
