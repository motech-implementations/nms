package org.motechproject.nms.rejectionhandler.service;

import org.motechproject.nms.rejectionhandler.domain.HealthSubFacilityImportRejection;

public interface HealthSubFacilityRejectionService {
    Long saveRejectedHealthSubFacility(HealthSubFacilityImportRejection healthSubFacilityImportRejection);
    void createRejectedHealthSubFacility(HealthSubFacilityImportRejection healthSubFacilityImportRejection);
    Long saveRejectedHealthSubFacilityInBulk(String rejectedHealthSubFacilityValues);
}
