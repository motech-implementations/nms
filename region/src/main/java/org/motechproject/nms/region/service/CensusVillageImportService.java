package org.motechproject.nms.region.service;

import org.motechproject.nms.region.domain.District;

public interface CensusVillageImportService extends LocationDataImportService {
    void addParent(District district);
}
