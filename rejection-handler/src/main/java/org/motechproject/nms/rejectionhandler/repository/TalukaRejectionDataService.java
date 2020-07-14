package org.motechproject.nms.rejectionhandler.repository;

import org.motechproject.mds.annotations.Lookup;
import org.motechproject.mds.annotations.LookupField;
import org.motechproject.mds.service.MotechDataService;
import org.motechproject.nms.rejectionhandler.domain.TalukaImportRejection;

public interface TalukaRejectionDataService extends MotechDataService<TalukaImportRejection> {
   @Lookup
   TalukaImportRejection findByUniqueCode(@LookupField(name = "stateId") Long stateId, @LookupField(name = "talukaCode") String talukaCode);

}
