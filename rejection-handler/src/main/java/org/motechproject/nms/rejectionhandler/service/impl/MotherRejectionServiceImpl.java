package org.motechproject.nms.rejectionhandler.service.impl;

import org.motechproject.mds.service.MotechDataService;
import org.motechproject.nms.rejectionhandler.domain.MotherImportAudit;
import org.motechproject.nms.rejectionhandler.repository.MotherRejectionDataService;
import org.motechproject.nms.rejectionhandler.service.MotherRejectionService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by beehyv on 17/7/17.
 */
public class MotherRejectionServiceImpl implements MotherRejectionService{

    @Autowired
    MotherRejectionDataService motherRejectionDataService;

    @Override
    public MotherImportAudit findByMotherId(String idNo, String registrationNo) {
        return motherRejectionDataService.findRejectedMother(idNo,registrationNo);
    }
}
