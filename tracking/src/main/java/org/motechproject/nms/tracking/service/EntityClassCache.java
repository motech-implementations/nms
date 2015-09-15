package org.motechproject.nms.tracking.service;

import org.springframework.cache.annotation.Cacheable;

public interface EntityClassCache {
    @Cacheable("entity-class")
    boolean isEntityInstance(String className);
}
