package org.motechproject.nms.region.repository;

import org.motechproject.mds.annotations.Lookup;
import org.motechproject.mds.annotations.LookupField;
import org.motechproject.mds.service.MotechDataService;
import org.motechproject.nms.region.domain.State;


public interface StateDataService extends MotechDataService<State> {
    @Lookup
    State findByName(@LookupField(name = "name") String name);

    @Lookup
    State findByCode(@LookupField(name = "code") Long code);
}
