package org.motechproject.nms.flw.repository;

import org.motechproject.mds.annotations.Lookup;
import org.motechproject.mds.annotations.LookupField;
import org.motechproject.mds.service.MotechDataService;
import org.motechproject.nms.flw.domain.FlwStatusUpdateAudit;
import org.motechproject.nms.flw.domain.UpdateStatusType;

import java.util.List;

/**
 * Data Service to CRUD on FlwStatusUpdateAudit
 */
public interface FlwStatusUpdateAuditDataService extends MotechDataService<FlwStatusUpdateAudit> {

    @Lookup
    List<FlwStatusUpdateAudit> findByFlwId(@LookupField(name = "flwId") String flwId);

    @Lookup
    List<FlwStatusUpdateAudit> findByContactNumber(@LookupField(name = "contactNumber") Long contactNumber);

    @Lookup
    List<FlwStatusUpdateAudit> findByUpdateStatusType(@LookupField(name = "updateStatusType") UpdateStatusType updateStatusType);

}
