package org.motechproject.nms.flw.repository;

import org.motechproject.mds.annotations.Lookup;
import org.motechproject.mds.annotations.LookupField;
import org.motechproject.mds.service.MotechDataService;
import org.motechproject.nms.flw.domain.FlwError;

import java.util.List;

/**
 * Data service to add flw sync error audit logs
 */
public interface FlwErrorDataService extends MotechDataService<FlwError> {

    @Lookup
    List<FlwError> findByMctsId(@LookupField(name = "mctsId") String mctsId,
                                @LookupField(name = "stateId") Long stateId,
                                @LookupField(name = "districtId") Long districtId);
}
