package org.motechproject.nms.tracking.repository;

import org.motechproject.mds.annotations.Lookup;
import org.motechproject.mds.annotations.LookupField;
import org.motechproject.mds.service.MotechDataService;
import org.motechproject.nms.tracking.domain.ChangeLog;

import java.util.List;

public interface ChangeLogDataService extends MotechDataService<ChangeLog> {

    @Lookup
    List<ChangeLog> findByEntityNameAndInstanceId(
            @LookupField(name = "entityName") String entityName,
            @LookupField(name = "instanceId") Long instanceId);
}
