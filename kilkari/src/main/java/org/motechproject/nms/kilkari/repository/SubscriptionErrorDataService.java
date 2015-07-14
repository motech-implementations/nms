package org.motechproject.nms.kilkari.repository;


import org.motechproject.mds.annotations.Lookup;
import org.motechproject.mds.annotations.LookupField;
import org.motechproject.mds.service.MotechDataService;
import org.motechproject.nms.kilkari.domain.SubscriptionError;

import java.util.List;

public interface SubscriptionErrorDataService extends MotechDataService<SubscriptionError> {
    @Lookup
    List<SubscriptionError> findByContactNumber(@LookupField(name = "contactNumber") Long contactNumber);
    
    @Lookup
    List<SubscriptionError> findByBeneficiaryId(@LookupField(name = "beneficiaryId") String beneficiaryId);
}
