package org.motechproject.nms.rejectionhandler.repository;

import org.motechproject.mds.annotations.Lookup;
import org.motechproject.mds.annotations.LookupField;
import org.motechproject.mds.service.MotechDataService;
import org.motechproject.nms.rejectionhandler.domain.MotherImportAudit;

/**
 * Created by beehyv on 17/7/17.
 */
public interface MotherRejectionDataService extends MotechDataService<MotherImportAudit> {

    @Lookup
    MotherImportAudit findRejectedMother(@LookupField(name = "idNo") String idNo,
                                         @LookupField(name = "registrationNo") String registrationNo);
}
