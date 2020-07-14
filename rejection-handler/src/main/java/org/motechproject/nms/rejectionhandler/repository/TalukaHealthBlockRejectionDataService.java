package org.motechproject.nms.rejectionhandler.repository;

import org.motechproject.mds.annotations.Lookup;
import org.motechproject.mds.annotations.LookupField;
import org.motechproject.mds.service.MotechDataService;
import org.motechproject.nms.rejectionhandler.domain.HealthSubFacilityImportRejection;
import org.motechproject.nms.rejectionhandler.domain.TalukaHealthBlockImportRejection;

public interface TalukaHealthBlockRejectionDataService extends MotechDataService<TalukaHealthBlockImportRejection> {

    @Lookup
    TalukaHealthBlockImportRejection findByUniqueCode(@LookupField(name = "healthBlockCode") Long healthBlockCode, @LookupField(name = "talukaCode") String talukaCode);


}
