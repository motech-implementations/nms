package org.motechproject.nms.rejectionhandler.repository;

import org.motechproject.mds.annotations.Lookup;
import org.motechproject.mds.annotations.LookupField;
import org.motechproject.mds.service.MotechDataService;
import org.motechproject.nms.rejectionhandler.domain.HealthBlockImportRejection;

public interface HealthBlockRejectionDataService extends MotechDataService<HealthBlockImportRejection> {

    @Lookup
    HealthBlockImportRejection findByUniqueCode(@LookupField(name = "stateId") Long stateId,@LookupField(name = "healthBlockCode") Long healthBlockCode);

}
