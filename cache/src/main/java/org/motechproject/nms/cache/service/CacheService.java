package org.motechproject.nms.cache.service;

/**
 * Cache module service interface
 */
public interface CacheService {

    void clear();

    interface Refresher {
        Object get(Object key);
    }

    void create(String cache, Refresher refresher);

    Object get(String cache, Object key);
}
