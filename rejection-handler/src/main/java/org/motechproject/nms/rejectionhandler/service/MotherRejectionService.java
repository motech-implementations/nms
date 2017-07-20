package org.motechproject.nms.rejectionhandler.service;

import org.motechproject.nms.rejectionhandler.domain.MotherImportAudit;

/**
 * Created by beehyv on 17/7/17.
 */
public interface MotherRejectionService {

    MotherImportAudit findByMotherId(String idNo,String registrationNo);
}
