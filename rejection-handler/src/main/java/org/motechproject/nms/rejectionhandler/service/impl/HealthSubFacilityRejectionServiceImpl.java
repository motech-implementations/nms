package org.motechproject.nms.rejectionhandler.service.impl;

import org.motechproject.nms.rejectionhandler.domain.HealthSubFacilityImportRejection;
import org.motechproject.nms.rejectionhandler.repository.HealthSubFacilityRejectionDataService;
import org.motechproject.nms.rejectionhandler.service.HealthSubFacilityRejectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("healthSubFacilityRejectionService")
public class HealthSubFacilityRejectionServiceImpl implements HealthSubFacilityRejectionService {
    @Autowired
    private HealthSubFacilityRejectionDataService healthSubFacilityRejectionDataService;

    @Override
    public void saveREjectedHealthSubFacility(HealthSubFacilityImportRejection healthSubFacilityImportRejection) {
        healthSubFacilityRejectionDataService.create(healthSubFacilityImportRejection);
    }
}
