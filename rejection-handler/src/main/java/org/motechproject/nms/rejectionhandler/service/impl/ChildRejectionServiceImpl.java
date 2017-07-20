package org.motechproject.nms.rejectionhandler.service.impl;

import org.motechproject.nms.rejectionhandler.domain.ChildImportAudit;
import org.motechproject.nms.rejectionhandler.repository.ChildRejectionDataService;
import org.motechproject.nms.rejectionhandler.service.ChildRejectionService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by beehyv on 20/7/17.
 */
public class ChildRejectionServiceImpl implements ChildRejectionService {

    @Autowired
    ChildRejectionDataService childRejectionDataService;

    @Override
    public ChildImportAudit findByChildId(String idNo, String registrationNo) {
        return childRejectionDataService.findRejectedChild(idNo,registrationNo);
    }
}
