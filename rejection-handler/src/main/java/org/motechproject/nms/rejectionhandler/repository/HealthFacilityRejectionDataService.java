package org.motechproject.nms.rejectionhandler.repository;

import org.motechproject.mds.annotations.Lookup;
import org.motechproject.mds.annotations.LookupField;
import org.motechproject.mds.service.MotechDataService;
import org.motechproject.nms.rejectionhandler.domain.HealthFacilityImportRejection;

public interface HealthFacilityRejectionDataService extends MotechDataService<HealthFacilityImportRejection> {

    @Lookup
    HealthFacilityImportRejection findByUniqueCode(@LookupField(name = "stateId") Long stateId,@LookupField(name = "healthFacilityCode") Long healthFacilityCode);

}
