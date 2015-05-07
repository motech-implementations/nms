package org.motechproject.nms.region.circle.repository;

import org.motechproject.mds.annotations.Lookup;
import org.motechproject.mds.annotations.LookupField;
import org.motechproject.mds.service.MotechDataService;
import org.motechproject.nms.region.circle.domain.Circle;

public interface CircleDataService  extends MotechDataService<Circle> {
    @Lookup
    Circle findByName(@LookupField(name = "name") String name);
}
