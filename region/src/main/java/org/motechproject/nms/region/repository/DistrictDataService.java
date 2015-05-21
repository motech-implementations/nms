package org.motechproject.nms.region.repository;

import org.motechproject.mds.annotations.Lookup;
import org.motechproject.mds.annotations.LookupField;
import org.motechproject.mds.service.MotechDataService;
import org.motechproject.nms.region.domain.District;

public interface DistrictDataService extends MotechDataService<District> {
    @Lookup
    District findByName(@LookupField(name = "name") String name);
}
