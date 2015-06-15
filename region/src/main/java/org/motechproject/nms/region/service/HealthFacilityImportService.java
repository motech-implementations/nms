package org.motechproject.nms.region.service;

import org.motechproject.nms.region.domain.Taluka;

public interface HealthFacilityImportService extends LocationDataImportService {
    void addParent(Taluka taluka);
}
