package org.motechproject.nms.flw.service;

import org.motechproject.mds.annotations.InstanceLifecycleListener;
import org.motechproject.mds.domain.InstanceLifecycleListenerType;
import org.motechproject.nms.flw.domain.FrontLineWorker;

import java.util.List;

/**
 * Simple example of a service interface.
 */
public interface FrontLineWorkerService {

    void add(FrontLineWorker frontLineWorker);

    FrontLineWorker getByContactNumber(Long contactNumber);

    List<FrontLineWorker> getRecords();

    void update(FrontLineWorker record);

    void delete(FrontLineWorker record);

    /**
     * Lifecycle listener that verifies a Front Line Worker can only be deleted if it is invalid
     * and has been in that state for 6 weeks
     *
     * @param frontLineWorker
     */
    @InstanceLifecycleListener(InstanceLifecycleListenerType.PRE_DELETE)
    void deleteAllowed(FrontLineWorker frontLineWorker);
}
