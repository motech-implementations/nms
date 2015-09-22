package org.motechproject.nms.region.service;

import org.motechproject.mds.annotations.InstanceLifecycleListener;
import org.motechproject.mds.annotations.InstanceLifecycleListenerType;
import org.motechproject.nms.region.domain.Circle;

import java.util.List;

public interface CircleService {
    Circle getByName(String name);
    List<Circle> getAll();

    /**
     * Evict the state cache if needed
     *
     */
    @InstanceLifecycleListener({InstanceLifecycleListenerType.POST_CREATE, InstanceLifecycleListenerType.PRE_DELETE,
            InstanceLifecycleListenerType.PRE_STORE})
    void cacheEvict(Circle circle);
}
