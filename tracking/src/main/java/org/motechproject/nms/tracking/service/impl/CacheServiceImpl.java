package org.motechproject.nms.tracking.service.impl;

import org.motechproject.mds.service.EntityService;
import org.motechproject.nms.tracking.service.CacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service("cacheService")
public class CacheServiceImpl implements CacheService {

    private EntityService entityService;

    private static final Logger LOGGER = LoggerFactory.getLogger(CacheServiceImpl.class);

    @Autowired
    public CacheServiceImpl(EntityService entityService) {
        this.entityService = entityService;
    }

    @Cacheable("entity-class")
    public boolean isEntityInstance(String className) {
        LOGGER.debug("*** NO CACHE {} ***", className);
        return entityService.getEntityByClassName(className) != null;
    }
}
