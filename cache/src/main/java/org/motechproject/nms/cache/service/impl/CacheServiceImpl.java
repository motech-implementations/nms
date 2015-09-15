package org.motechproject.nms.cache.service.impl;

import org.motechproject.nms.cache.service.CacheService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service("cacheService")
public class CacheServiceImpl implements CacheService {

    private Map<String, Map<Object, Object>> caches;
    private Map<String, Refresher> refreshers;

    public void clear() {
        caches = new HashMap<>();
        refreshers = new HashMap<>();
    }

    public void create(String cache, Refresher refresher) {
        if (caches == null) {
            clear();
        }
        refreshers.put(cache, refresher);
        caches.put(cache, new HashMap<Object, Object>());
    }

    public Object get(String cache, Object key) {
        if (caches == null) {
            throw new IllegalStateException("No cache was registered.");
        }

        if (!caches.containsKey(cache)) {
            throw new IllegalStateException(String.format("No cache found for '%s'.", cache));
        }

        Map<Object, Object> map = caches.get(cache);

        if (!map.containsKey(key)) {
            Refresher refresher = refreshers.get(cache);
            Object value = refresher.get(key);
            map.put(key, refresher.get(key));
            return value;
        }

        return map.get(key);
    }
}

