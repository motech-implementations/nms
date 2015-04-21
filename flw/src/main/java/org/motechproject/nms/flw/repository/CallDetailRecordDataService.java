package org.motechproject.nms.flw.repository;

import org.motechproject.mds.annotations.Lookup;
import org.motechproject.mds.annotations.LookupField;
import org.motechproject.mds.service.MotechDataService;
import org.motechproject.nms.flw.domain.CallDetailRecord;

public interface CallDetailRecordDataService extends MotechDataService<CallDetailRecord> {
    @Lookup
    CallDetailRecord findByCallingNumber(@LookupField(name = "callingNumber") Long callingNumber);
}
