package org.motechproject.nms.region.service.impl;

import org.motechproject.nms.region.domain.State;
import org.motechproject.nms.region.repository.StateDataService;
import org.motechproject.nms.region.service.StateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("stateService")
public class StateServiceImpl implements StateService {
    private StateDataService stateDataService;

    @Autowired
    public StateServiceImpl(StateDataService stateDataService) {
        this.stateDataService = stateDataService;
    }

    @Override
    public State findByName(String name) {
        return stateDataService.findByName(name);
    }

    @Override
    public State findByCode(Long code) {
        return stateDataService.findByCode(code);
    }

    @Override
    public List<State> retrieveAll() {
        return stateDataService.retrieveAll();
    }

    @Override
    public State create(State state) {
        return stateDataService.create(state);
    }

    @Override
    public State update(State state) {
        return stateDataService.update(state);
    }

    @Override
    public void delete(State state) {
        stateDataService.delete(state);
    }

    @Override
    public void deleteAll() {
        stateDataService.deleteAll();
    }
}
