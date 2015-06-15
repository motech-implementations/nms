package org.motechproject.nms.region.service;

import org.motechproject.nms.region.domain.District;

public interface NonCensusVillageImportService extends LocationDataImportService {
    void addParent(District district);
}
