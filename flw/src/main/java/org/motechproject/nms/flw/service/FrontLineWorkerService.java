package org.motechproject.nms.flw.service;

import org.motechproject.event.MotechEvent;
import org.motechproject.mds.annotations.InstanceLifecycleListener;
import org.motechproject.mds.domain.InstanceLifecycleListenerType;
import org.motechproject.nms.flw.domain.FrontLineWorker;
import org.motechproject.nms.region.domain.State;

import java.util.List;

/**
 * Simple example of a service interface.
 */
public interface FrontLineWorkerService {

    State getState(FrontLineWorker frontLineWorker);

    void add(FrontLineWorker frontLineWorker);

    FrontLineWorker getByContactNumber(Long contactNumber);

    FrontLineWorker getByFlwId(String flwId);

    FrontLineWorker getByMctsFlwId(String mctsFlwId);

    List<FrontLineWorker> getRecords();

    void update(FrontLineWorker record);

    void delete(FrontLineWorker record);

    /**
     * MotechEvent handler that responds to scheduler events.  Purges FLW records that are in invalid state
     * and have been for more than flw.weeks_to_keep_invalid_flws weeks
     *
     * @param event
     */
    void purgeOldInvalidFLWs(MotechEvent event);

    /**
     * Lifecycle listener that verifies a Front Line Worker can only be deleted if it is invalid
     * and has been in that state for 6 weeks
     *
     * @param frontLineWorker
     */
    @InstanceLifecycleListener(InstanceLifecycleListenerType.PRE_DELETE)
    void deletePreconditionCheck(FrontLineWorker frontLineWorker);
}
