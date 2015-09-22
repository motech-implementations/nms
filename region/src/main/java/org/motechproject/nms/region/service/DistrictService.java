package org.motechproject.nms.region.service;

import org.motechproject.mds.annotations.InstanceLifecycleListener;
import org.motechproject.mds.annotations.InstanceLifecycleListenerType;
import org.motechproject.nms.region.domain.District;
import org.motechproject.nms.region.domain.Language;
import org.motechproject.nms.region.domain.State;

import java.util.Set;

public interface DistrictService {
    Set<District> getAllForLanguage(Language language);
    District findByStateAndCode(State state, Long code);
    District findByStateAndName(State state, String name);
    District create(District district);
    District update(District district);

    /**
     * Evict the state cache if needed
     *
     */
    @InstanceLifecycleListener({InstanceLifecycleListenerType.POST_CREATE, InstanceLifecycleListenerType.PRE_DELETE,
            InstanceLifecycleListenerType.PRE_STORE})
    void cacheEvict(District district);
}
