package org.motechproject.nms.region.utils;

import org.motechproject.nms.region.domain.State;
import org.motechproject.nms.region.repository.StateDataService;

public final class GetStateByCode extends GetInstanceByLong<State> {

    private StateDataService stateDataService;

    public GetStateByCode(StateDataService stateDataService) {
        this.stateDataService = stateDataService;
    }

    @Override
    public State retrieve(Long value) {
        return stateDataService.findByCode(value);
    }

}
