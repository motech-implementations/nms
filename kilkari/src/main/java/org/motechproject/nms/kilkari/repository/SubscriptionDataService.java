package org.motechproject.nms.kilkari.repository;

import org.joda.time.DateTime;
import org.motechproject.mds.annotations.Lookup;
import org.motechproject.mds.annotations.LookupField;
import org.motechproject.mds.query.QueryParams;
import org.motechproject.mds.service.MotechDataService;
import org.motechproject.mds.util.Constants;
import org.motechproject.nms.kilkari.domain.Subscription;
import org.motechproject.nms.kilkari.domain.SubscriptionStatus;

import java.util.List;

public interface SubscriptionDataService extends MotechDataService<Subscription> {
    @Lookup
    Subscription findBySubscriptionId(@LookupField(name = "subscriptionId") String subscriptionId);

    @Lookup
    List<Subscription> findByStatus(@LookupField(name = "status")SubscriptionStatus status);

    @Lookup
    List<Subscription> findByStatusAndStartDate(
            @LookupField(name = "status")SubscriptionStatus status,
            @LookupField(name = "startDate", customOperator = Constants.Operators.LT_EQ)DateTime startDate,
            QueryParams queryParams);
}
