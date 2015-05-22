package org.motechproject.nms.region.repository;

import org.motechproject.mds.annotations.Lookup;
import org.motechproject.mds.annotations.LookupField;
import org.motechproject.mds.service.MotechDataService;
import org.motechproject.nms.region.domain.HealthFacility;

public interface HealthFacilityDataService extends MotechDataService<HealthFacility> {
    @Lookup
    HealthFacility findByCode(@LookupField(name = "code") Long code);
}
