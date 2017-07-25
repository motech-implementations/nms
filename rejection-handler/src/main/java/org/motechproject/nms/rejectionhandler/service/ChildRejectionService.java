package org.motechproject.nms.rejectionhandler.service;

import org.motechproject.nms.rejectionhandler.domain.ChildImportRejection;

/**
 * Created by beehyv on 20/7/17.
 */
public interface ChildRejectionService {

    ChildImportRejection findByChildId(String idNo, String registrationNo);

    void createOrUpdateChild(ChildImportRejection childImportRejection);
}
