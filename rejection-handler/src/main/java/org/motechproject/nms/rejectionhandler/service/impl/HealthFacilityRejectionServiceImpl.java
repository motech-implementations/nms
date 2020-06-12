package org.motechproject.nms.rejectionhandler.service.impl;

import org.motechproject.nms.rejectionhandler.domain.HealthFacilityImportRejection;
import org.motechproject.nms.rejectionhandler.repository.HealthFacilityRejectionDataService;
import org.motechproject.nms.rejectionhandler.service.HealthFacilityRejectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("healthFacilityRejectionService")
public class HealthFacilityRejectionServiceImpl implements HealthFacilityRejectionService {
    @Autowired
    HealthFacilityRejectionDataService healthFacilityRejectionDataService;


    @Override
    public void saveRejectedHealthFacility(HealthFacilityImportRejection healthFacilityImportRejection) {
        if(   healthFacilityRejectionDataService.findByUniqueCode(healthFacilityImportRejection.getStateId(),healthFacilityImportRejection.getHealthFacilityCode()) == null){
            healthFacilityRejectionDataService.create(healthFacilityImportRejection);
        }
        else {
            healthFacilityRejectionDataService.update(healthFacilityImportRejection);
        }
    }
    @Override
    public void createRejectedHealthFacility(HealthFacilityImportRejection healthFacilityImportRejection) {
        healthFacilityRejectionDataService.create(healthFacilityImportRejection);
    }
}
