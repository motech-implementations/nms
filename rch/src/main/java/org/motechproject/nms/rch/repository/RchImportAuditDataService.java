package org.motechproject.nms.rch.repository;

import org.motechproject.mds.service.MotechDataService;
import org.motechproject.nms.rch.domain.RchImportAudit;
import org.joda.time.LocalDate;
import org.motechproject.mds.annotations.Lookup;
import org.motechproject.mds.annotations.LookupField;
import org.motechproject.nms.rch.domain.RchUserType;

import java.util.List;


/**
 * Data service to CRUD on RCH import audit
 */
public interface RchImportAuditDataService extends MotechDataService<RchImportAudit> {

    @Lookup
    List<RchImportAudit> findByStateCodeAndDateRangeAndUserType(
            @LookupField(name = "stateCode") Long stateCode,
            @LookupField(name = "startImportDate") LocalDate startImportDate,
            @LookupField(name = "endImportDate") LocalDate endImportDate,
            @LookupField(name = "userType") RchUserType userType);


}
