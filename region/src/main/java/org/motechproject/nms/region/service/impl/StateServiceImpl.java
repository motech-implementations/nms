package org.motechproject.nms.region.service.impl;

import org.motechproject.nms.region.domain.State;
import org.motechproject.nms.region.repository.StateDataService;
import org.motechproject.nms.region.service.StateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("stateService")
public class StateServiceImpl implements StateService {
    private StateDataService stateDataService;

    private static final Logger LOGGER = LoggerFactory.getLogger(StateServiceImpl.class);

    @Autowired
    public StateServiceImpl(StateDataService stateDataService) {
        this.stateDataService = stateDataService;
    }

    @Override
    @Cacheable("state-name")
    public State findByName(String name) {
        LOGGER.debug("*** NO CACHE findByName({}) ***", name);
        return stateDataService.findByName(name);
    }

    @Override
    @Cacheable("state-code")
    public State findByCode(Long code) {
        LOGGER.debug("*** NO CACHE findByCode({}) ***", code);
        return stateDataService.findByCode(code);
    }

    @Override
    @Cacheable("state-all")
    public List<State> retrieveAll() {
        LOGGER.debug("*** NO CACHE getAll() ***");
        return stateDataService.retrieveAll();
    }

    @Override
    @CacheEvict(value = {"state-code", "state-name"}, allEntries = true)
    public State create(State state) {
        return stateDataService.create(state);
    }

    @Override
    @CacheEvict(value = {"state-code", "state-name"}, allEntries = true)
    public State update(State state) {
        return stateDataService.update(state);
    }

    @Override
    @CacheEvict(value = {"state-code", "state-name"}, allEntries = true)
    public void delete(State state) {
        stateDataService.delete(state);
    }

    @Override
    @CacheEvict(value = {"state-code", "state-name"}, allEntries = true)
    public void deleteAll() {
        stateDataService.deleteAll();
    }

    /*
     *
     * Only to be called by ITs
     *
     */
    @Override
    @CacheEvict(value = {"state-code", "state-name"}, allEntries = true)
    public void cacheEvict() { }
}
