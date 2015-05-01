package org.motechproject.nms.kilkari.repository;

import org.motechproject.mds.annotations.Lookup;
import org.motechproject.mds.annotations.LookupField;
import org.motechproject.mds.service.MotechDataService;
import org.motechproject.nms.kilkari.domain.SubscriptionPack;

public interface SubscriptionPackDataService extends MotechDataService<SubscriptionPack> {
    @Lookup
    SubscriptionPack byName(@LookupField(name = "name") String name);
}
