package org.motechproject.nms.rejectionhandler.repository;

import org.motechproject.mds.annotations.Lookup;
import org.motechproject.mds.annotations.LookupField;
import org.motechproject.mds.service.MotechDataService;
import org.motechproject.nms.rejectionhandler.domain.TalukaImportRejection;
import org.motechproject.nms.rejectionhandler.domain.VillageHealthSubFacilityImportRejection;

public interface VillageHealthSubFacilityRejectionDataService extends MotechDataService<VillageHealthSubFacilityImportRejection> {
    @Lookup
    VillageHealthSubFacilityImportRejection findByUniqueCode(@LookupField(name = "villageCode") Long villageCode, @LookupField(name = "healthSubFacilityCode") Long healthSubFacilityCode);

}
