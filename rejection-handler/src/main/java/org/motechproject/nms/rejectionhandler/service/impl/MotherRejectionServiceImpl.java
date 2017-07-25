package org.motechproject.nms.rejectionhandler.service.impl;

import org.motechproject.nms.rejectionhandler.domain.MotherImportRejection;
import org.motechproject.nms.rejectionhandler.repository.MotherRejectionDataService;
import org.motechproject.nms.rejectionhandler.service.MotherRejectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

import static org.motechproject.nms.tracking.utils.TrackChangeUtils.LOGGER;

/**
 * Created by beehyv on 17/7/17.
 */
@Service("motherRejectionService")
public class MotherRejectionServiceImpl implements MotherRejectionService{

    @Autowired
    private MotherRejectionDataService motherRejectionDataService;

    @Override
    public MotherImportRejection findByMotherId(String idNo, String registrationNo) {
        return motherRejectionDataService.findRejectedMother(idNo,registrationNo);
    }

    @Override
    public void createOrUpdateMother(MotherImportRejection motherImportRejection) {
        MotherImportRejection motherImportRejection1 = motherRejectionDataService.findRejectedMother(motherImportRejection.getIdNo(), motherImportRejection.getRegistrationNo());

        if(motherImportRejection1 == null && !motherImportRejection.getAccepted()){
            motherImportRejection.setAction("CREATED");
            motherImportRejection.setCreationDate(new Date());
            motherImportRejection.setModificationDate(new Date());
            motherRejectionDataService.create(motherImportRejection);
        } else if(motherImportRejection1 == null && motherImportRejection.getAccepted()){
            LOGGER.debug(String.format("There is no mother rejection data for mctsId %s and rchId %s", motherImportRejection.getIdNo(), motherImportRejection.getRegistrationNo()));
        } else if(motherImportRejection1 != null && !motherImportRejection1.getAccepted()){
            motherImportRejection.setModificationDate(new Date());
            motherRejectionDataService.update(motherImportRejection);
        } else if(motherImportRejection1 != null && motherImportRejection1.getAccepted()){
            motherImportRejection.setModificationDate(new Date());
            motherImportRejection.setAction("UPDATED");
            motherRejectionDataService.update(motherImportRejection);
        }
    }
}
