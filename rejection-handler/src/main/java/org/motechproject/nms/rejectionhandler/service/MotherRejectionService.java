package org.motechproject.nms.rejectionhandler.service;

import org.motechproject.nms.rejectionhandler.domain.MotherImportRejection;

/**
 * Created by beehyv on 17/7/17.
 */
public interface MotherRejectionService {

    MotherImportRejection findByMotherId(String idNo, String registrationNo);

    void createOrUpdateMother(MotherImportRejection motherImportRejection);
}
