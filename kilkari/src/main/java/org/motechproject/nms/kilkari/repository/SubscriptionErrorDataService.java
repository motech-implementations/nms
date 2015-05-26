package org.motechproject.nms.kilkari.repository;


import org.motechproject.mds.annotations.Lookup;
import org.motechproject.mds.annotations.LookupField;
import org.motechproject.mds.service.MotechDataService;
import org.motechproject.nms.kilkari.domain.SubscriptionError;

public interface SubscriptionErrorDataService extends MotechDataService<SubscriptionError> {
    @Lookup
    SubscriptionError findByContactNumber(@LookupField(name = "contactNumber") Long contactNumber);
}
