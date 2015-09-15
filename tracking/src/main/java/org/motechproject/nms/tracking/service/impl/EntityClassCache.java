package org.motechproject.nms.tracking.service.impl;

import org.motechproject.mds.service.EntityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;

public class EntityClassCache {
    private EntityService entityService;

    private static final Logger LOGGER = LoggerFactory.getLogger(EntityClassCache.class);

    public EntityClassCache(EntityService entityService) {
        this.entityService = entityService;
    }

    @Cacheable("entity-class")
    public boolean isEntityInstance(String className) {
        LOGGER.debug("*** NO CACHE {} ***", className);
        return entityService.getEntityByClassName(className) != null;
    }


}
