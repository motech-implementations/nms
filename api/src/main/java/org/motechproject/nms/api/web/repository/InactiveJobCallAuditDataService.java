package org.motechproject.nms.api.web.repository;

import org.motechproject.mds.annotations.Lookup;
import org.motechproject.mds.annotations.LookupField;
import org.motechproject.mds.service.MotechDataService;
import org.motechproject.nms.api.web.domain.InactiveJobCallAudit;

import java.util.List;

/**
 * Data service to CRUD on Inactive Job Flw Call audit
 */

public interface InactiveJobCallAuditDataService extends MotechDataService<InactiveJobCallAudit> {

    @Lookup
    List<InactiveJobCallAudit> findByNumber(@LookupField(name = "callingNumber") Long callingNumber);

    @Lookup
    List<InactiveJobCallAudit> findByFlwId(@LookupField(name = "flwId") String flwId);
}
