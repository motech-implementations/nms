package org.motechproject.nms.region.service;

import org.motechproject.mds.annotations.InstanceLifecycleListener;
import org.motechproject.mds.annotations.InstanceLifecycleListenerType;
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

    /**
     * Evict the state cache if needed
     *
     */
    @InstanceLifecycleListener({InstanceLifecycleListenerType.POST_CREATE, InstanceLifecycleListenerType.PRE_DELETE,
            InstanceLifecycleListenerType.PRE_STORE})
    void cacheEvict(State state);
}
