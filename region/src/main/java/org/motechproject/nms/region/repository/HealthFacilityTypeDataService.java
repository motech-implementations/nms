package org.motechproject.nms.region.repository;

import org.motechproject.mds.annotations.Lookup;
import org.motechproject.mds.annotations.LookupField;
import org.motechproject.mds.service.MotechDataService;
import org.motechproject.nms.region.domain.HealthFacilityType;


public interface HealthFacilityTypeDataService extends MotechDataService<HealthFacilityType> {
    @Lookup
    HealthFacilityType findByCode(@LookupField(name = "code") Long code);
}
