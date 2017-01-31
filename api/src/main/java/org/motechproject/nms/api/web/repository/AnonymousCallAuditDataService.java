package org.motechproject.nms.api.web.repository;

import org.motechproject.mds.annotations.Lookup;
import org.motechproject.mds.annotations.LookupField;
import org.motechproject.mds.service.MotechDataService;
import org.motechproject.nms.api.web.domain.AnonymousCallAudit;

import java.util.List;

/**
 * Data service to CRUD on Anonymous Call Details audit
 */
public interface AnonymousCallAuditDataService extends MotechDataService<AnonymousCallAudit> {

    @Lookup
    List<AnonymousCallAudit> findByNumber(@LookupField(name = "callingNumber") Long callingNumber);
}
