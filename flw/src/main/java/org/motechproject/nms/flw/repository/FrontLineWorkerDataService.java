package org.motechproject.nms.flw.repository;

import org.motechproject.nms.flw.domain.FrontLineWorker;

import org.motechproject.mds.annotations.Lookup;
import org.motechproject.mds.annotations.LookupField;
import org.motechproject.mds.service.MotechDataService;

public interface FrontLineWorkerDataService extends MotechDataService<FrontLineWorker> {
    @Lookup
    FrontLineWorker findByContactNumber(@LookupField(name = "contactNumber") Long contactNumber);
}
