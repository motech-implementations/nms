package org.motechproject.nms.region.service;

import org.motechproject.nms.region.domain.State;

import java.util.List;

public interface StateService {
    State findByName(String name);
    State findByCode(Long code);
    List<State>  retrieveAll();
    State create(State state);
    State update(State state);
    void delete(State state);
    void deleteAll();
}
