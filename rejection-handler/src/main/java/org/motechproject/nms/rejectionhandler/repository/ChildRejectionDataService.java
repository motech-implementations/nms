package org.motechproject.nms.rejectionhandler.repository;

import org.motechproject.mds.annotations.Lookup;
import org.motechproject.mds.annotations.LookupField;
import org.motechproject.mds.service.MotechDataService;
import org.motechproject.nms.rejectionhandler.domain.ChildImportRejection;

/**
 * Created by beehyv on 19/7/17.
 */
public interface ChildRejectionDataService extends MotechDataService<ChildImportRejection> {

    @Lookup
    ChildImportRejection findByIdno(@LookupField(name = "idNo") String idNo);

    @Lookup
    ChildImportRejection findByRegistrationNo(@LookupField(name = "registrationNo") String registrationNo);


}


