package org.motechproject.nms.rejectionhandler.repository;

import org.motechproject.mds.annotations.Lookup;
import org.motechproject.mds.annotations.LookupField;
import org.motechproject.mds.service.MotechDataService;
import org.motechproject.nms.rejectionhandler.domain.HealthFacilityImportRejection;
import org.motechproject.nms.rejectionhandler.domain.HealthSubFacilityImportRejection;

public interface HealthSubFacilityRejectionDataService extends MotechDataService<HealthSubFacilityImportRejection> {
    @Lookup
    HealthSubFacilityImportRejection findByUniqueCode(@LookupField(name = "stateId") Long stateId, @LookupField(name = "healthSubFacilityCode") Long healthSubFacilityCode);

}
