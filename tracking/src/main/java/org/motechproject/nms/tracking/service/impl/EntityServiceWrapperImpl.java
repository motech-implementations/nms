package org.motechproject.nms.tracking.service.impl;

import org.motechproject.mds.service.EntityService;
import org.motechproject.nms.tracking.service.EntityServiceWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service("entityServiceWrapper")
public class EntityServiceWrapperImpl implements EntityServiceWrapper {

    private EntityService entityService;

    @Autowired
    public EntityServiceWrapperImpl(EntityService entityService) {
        this.entityService = entityService;
    }

    @Cacheable("entity-class")
    public boolean isEntityInstance(String className) {
        return entityService.getEntityByClassName(className) != null;
    }
}
