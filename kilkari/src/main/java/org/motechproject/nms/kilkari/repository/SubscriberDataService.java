package org.motechproject.nms.kilkari.repository;

import org.motechproject.nms.kilkari.domain.Subscriber;

import org.motechproject.mds.annotations.Lookup;
import org.motechproject.mds.annotations.LookupField;
import org.motechproject.mds.service.MotechDataService;

public interface SubscriberDataService extends MotechDataService<Subscriber> {
    @Lookup
    Subscriber findByCallingNumber(@LookupField(name = "callingNumber") String callingNumber);
}
