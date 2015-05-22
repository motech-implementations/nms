package org.motechproject.nms.region.repository;

import org.motechproject.mds.annotations.Lookup;
import org.motechproject.mds.annotations.LookupField;
import org.motechproject.mds.service.MotechDataService;
import org.motechproject.nms.region.domain.HealthSubFacility;

public interface HealthSubFacilityDataService extends MotechDataService<HealthSubFacility> {
    @Lookup
    HealthSubFacility findByCode(@LookupField(name = "code") Long code);
}
