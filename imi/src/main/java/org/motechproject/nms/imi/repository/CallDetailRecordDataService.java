package org.motechproject.nms.imi.repository;

import org.motechproject.mds.annotations.Lookup;
import org.motechproject.mds.annotations.LookupField;
import org.motechproject.mds.service.MotechDataService;
import org.motechproject.nms.imi.domain.CallDetailRecord;

import java.util.List;

public interface CallDetailRecordDataService  extends MotechDataService<CallDetailRecord> {

    @Lookup
    List<CallDetailRecord> findByRequestIdAndCallId(@LookupField(name = "requestId") String requestId,
                                                    @LookupField(name = "callId") String callId);
}
