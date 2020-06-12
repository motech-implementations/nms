package org.motechproject.nms.rejectionhandler.repository;

import org.motechproject.mds.annotations.Lookup;
import org.motechproject.mds.annotations.LookupField;
import org.motechproject.mds.service.MotechDataService;
import org.motechproject.nms.rejectionhandler.domain.VillageHealthSubFacilityImportRejection;
import org.motechproject.nms.rejectionhandler.domain.VillageImportRejection;

public interface VillageRejectionDataService extends MotechDataService<VillageImportRejection> {
    @Lookup
    VillageImportRejection findByUniqueCode(@LookupField(name = "stateId") Long stateId, @LookupField(name = "villageCode") Long villageCode);

}
