package org.motechproject.nms.tracking.service.impl;

import org.motechproject.mds.service.EntityService;
import org.motechproject.nms.tracking.service.EntityClassCache;

public class EntityClassCacheImpl implements EntityClassCache {
    private EntityService entityService;


    public EntityClassCacheImpl(EntityService entityService) {
        this.entityService = entityService;
    }

    public boolean isEntityInstance(String className) {
        return entityService.getEntityByClassName(className) != null;
    }
}
