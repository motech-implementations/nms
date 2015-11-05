package org.motechproject.nms.region.service;

import org.motechproject.nms.region.domain.HealthFacility;
import org.motechproject.nms.region.domain.HealthSubFacility;

public interface HealthSubFacilityService {
    HealthSubFacility findByHealthFacilityAndCode(HealthFacility healthFacility, Long code);
    HealthSubFacility create(HealthSubFacility healthSubFacility);
    HealthSubFacility update(HealthSubFacility healthSubFacility);
}
