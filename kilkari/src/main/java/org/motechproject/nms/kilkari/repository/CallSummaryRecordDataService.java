package org.motechproject.nms.kilkari.repository;

import org.motechproject.mds.annotations.Lookup;
import org.motechproject.mds.annotations.LookupField;
import org.motechproject.mds.service.MotechDataService;
import org.motechproject.mds.util.Constants;
import org.motechproject.nms.kilkari.domain.CallSummaryRecord;

import java.util.List;

public interface CallSummaryRecordDataService extends MotechDataService<CallSummaryRecord> {

    @Lookup
    CallSummaryRecord findBySubscriptionId(@LookupField(name = "subscriptionId") String subscriptionId);

    @Lookup
    List<CallSummaryRecord> findLikeSubscriptionId(@LookupField(name = "subscriptionId",
            customOperator = Constants.Operators.MATCHES) String subscriptionId);

    long countFindLikeSubscriptionId(@LookupField(name = "subscriptionId") String subscriptionId);

}
