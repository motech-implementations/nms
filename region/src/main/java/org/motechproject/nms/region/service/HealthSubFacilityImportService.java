package org.motechproject.nms.region.service;

import org.motechproject.nms.region.domain.HealthBlock;

public interface HealthSubFacilityImportService extends LocationDataImportService {
    void addParent(HealthBlock healthBlock);
}
