package org.motechproject.nms.flw.repository;

import org.motechproject.mds.annotations.Lookup;
import org.motechproject.mds.annotations.LookupField;
import org.motechproject.mds.service.MotechDataService;
import org.motechproject.nms.flw.domain.FlwJobStatus;
import org.motechproject.nms.flw.domain.FrontLineWorker;

import java.util.List;

public interface FrontLineWorkerDataService extends MotechDataService<FrontLineWorker> {
    @Lookup
    FrontLineWorker findByFlwId(@LookupField(name = "flwId") String flwId);

    @Lookup
    FrontLineWorker findByContactNumber(@LookupField(name = "contactNumber") Long contactNumber);

    @Lookup
    List<FrontLineWorker> findListByContactNumber(@LookupField(name = "contactNumber") Long contactNumber);

    @Lookup
    FrontLineWorker findByContactNumberAndJobStatus(@LookupField(name = "contactNumber") Long contactNumber,
                                                    @LookupField(name = "jobStatus") FlwJobStatus jobStatus);
}
