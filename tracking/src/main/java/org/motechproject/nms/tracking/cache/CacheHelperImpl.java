package org.motechproject.nms.tracking.cache;

import org.motechproject.mds.service.EntityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service("cacheHelper")
public class CacheHelperImpl implements CacheHelper {

    private EntityService entityService;

    private static final Logger LOGGER = LoggerFactory.getLogger(CacheHelperImpl.class);

    @Autowired
    public CacheHelperImpl(EntityService entityService) {
        this.entityService = entityService;
    }

    @Cacheable("entity-class")
    public boolean isEntityInstance(String className) {
        LOGGER.debug("*** NO CACHE {} ***", className);
        return entityService.getEntityByClassName(className) != null;
    }
}
