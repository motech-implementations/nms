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
        healthFacilityRejectionDataService.create(healthFacilityImportRejection);
    }
}
