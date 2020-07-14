package org.motechproject.nms.rejectionhandler.repository;

import org.motechproject.mds.annotations.Lookup;
import org.motechproject.mds.annotations.LookupField;
import org.motechproject.mds.service.MotechDataService;
import org.motechproject.nms.rejectionhandler.domain.DistrictImportRejection;

public interface DistrictRejectionDataService extends MotechDataService<DistrictImportRejection> {

    @Lookup
    DistrictImportRejection findByUniqueCode(@LookupField(name = "stateId") Long stateId,@LookupField(name = "districtCode") Long districtCode);

}
