package org.motechproject.nms.rejectionhandler.service;

import org.motechproject.nms.rejectionhandler.domain.ChildImportAudit;

/**
 * Created by beehyv on 20/7/17.
 */
public interface ChildRejectionService {

    ChildImportAudit findByChildId(String idNo,String registrationNo);
}
