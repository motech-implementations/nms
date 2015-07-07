package org.motechproject.nms.flw.repository;

import org.motechproject.mds.annotations.Lookup;
import org.motechproject.mds.annotations.LookupField;
import org.motechproject.mds.service.MotechDataService;
import org.motechproject.nms.flw.domain.FrontLineWorker;

public interface FrontLineWorkerDataService extends MotechDataService<FrontLineWorker> {
    @Lookup
    FrontLineWorker findByFlwId(@LookupField(name = "flwId") String flwId);

    @Lookup
    FrontLineWorker findByContactNumber(@LookupField(name = "contactNumber") Long contactNumber);
}
