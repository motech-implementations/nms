package org.motechproject.nms.rejectionhandler.service;

import org.motechproject.nms.rejectionhandler.domain.HealthFacilityImportRejection;

public interface HealthFacilityRejectionService {

    void createRejectedHealthFacility(HealthFacilityImportRejection healthFacilityImportRejection);

    void saveRejectedHealthFacility(HealthFacilityImportRejection healthFacilityImportRejection);
}
