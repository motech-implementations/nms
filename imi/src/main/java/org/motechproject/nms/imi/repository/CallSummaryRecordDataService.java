package org.motechproject.nms.imi.repository;

import org.motechproject.mds.annotations.Lookup;
import org.motechproject.mds.annotations.LookupField;
import org.motechproject.mds.service.MotechDataService;
import org.motechproject.nms.imi.domain.CallSummaryRecord;

import java.util.List;

public interface CallSummaryRecordDataService  extends MotechDataService<CallSummaryRecord> {
    @Lookup
    List<CallSummaryRecord> findByRequestId(@LookupField(name = "requestId") String requestId);

    long countFindByRequestId(@LookupField(name = "requestId") String requestId);
}
