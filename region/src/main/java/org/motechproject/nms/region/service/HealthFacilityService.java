package org.motechproject.nms.region.service;

import org.motechproject.nms.region.domain.HealthBlock;
import org.motechproject.nms.region.domain.HealthFacility;

public interface HealthFacilityService {
    HealthFacility findByHealthBlockAndCode(HealthBlock healthBlock, Long code);
    HealthFacility create(HealthFacility healthFacility);
    HealthFacility update(HealthFacility healthFacility);
}
