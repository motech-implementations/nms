package org.motechproject.nms.region.service.impl;

import org.motechproject.nms.region.domain.Circle;
import org.motechproject.nms.region.repository.CircleDataService;
import org.motechproject.nms.region.service.CircleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("circleService")
public class CircleServiceImpl implements CircleService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CircleServiceImpl.class);

    @Autowired
    private CircleDataService circleDataService;

    /**
     * Returns the circle for a given name
     *
     * @param name the circle name
     * @return the circle object if found
     */
    @Override
    @Cacheable("circle-name")
    public Circle getByName(String name) {
        LOGGER.debug("*** CACHE EVICT getByName({}) ***", name);
        return circleDataService.findByName(name);
    }

    /**
     * Returns all circles in the database
     *
     * @return all the circles in the database
     */
    @Override
    @Cacheable("circle-all")
    public List<Circle> getAll() {
        LOGGER.debug("*** NO CACHE getAll() ***");
        return circleDataService.retrieveAll();
    }

    @Override
    @CacheEvict(value = {"circle-name", "circle-name" }, allEntries = true)
    public void cacheEvict(Circle circle) {
        LOGGER.debug("*** CACHE EVICT circle={} ***", circle);
    }
}
