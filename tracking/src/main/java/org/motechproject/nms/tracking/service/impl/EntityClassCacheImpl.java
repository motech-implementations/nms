package org.motechproject.nms.tracking.service.impl;

import org.motechproject.mds.service.EntityService;
import org.motechproject.nms.tracking.service.EntityClassCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EntityClassCacheImpl implements EntityClassCache {
    private EntityService entityService;

    private static final Logger LOGGER = LoggerFactory.getLogger(EntityClassCacheImpl.class);

    public EntityClassCacheImpl(EntityService entityService) {
        this.entityService = entityService;
    }

    public boolean isEntityInstance(String className) {
        LOGGER.debug("*** NO CACHE {} ***", className);
        return entityService.getEntityByClassName(className) != null;
    }
}
