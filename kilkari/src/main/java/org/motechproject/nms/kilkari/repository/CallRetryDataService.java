package org.motechproject.nms.kilkari.repository;

import org.motechproject.mds.annotations.Lookup;
import org.motechproject.mds.annotations.LookupField;
import org.motechproject.mds.service.MotechDataService;
import org.motechproject.nms.kilkari.domain.CallRetry;

public interface CallRetryDataService extends MotechDataService<CallRetry> {
    @Lookup
    CallRetry findBySubscriptionId(@LookupField(name = "subscriptionId") String subscriptionId);
}
