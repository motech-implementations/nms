package org.motechproject.nms.location.repository;

import org.motechproject.mds.annotations.Lookup;
import org.motechproject.mds.annotations.LookupField;
import org.motechproject.mds.service.MotechDataService;
import org.motechproject.nms.location.domain.State;


public interface StateDataService extends MotechDataService<State> {
    @Lookup
    State findByCode(@LookupField(name = "code") Long code);
}
