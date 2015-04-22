package org.motechproject.nms.flw.service;

import org.motechproject.nms.flw.domain.Service;
import org.motechproject.nms.flw.domain.ServiceUsageCap;
import org.motechproject.nms.location.domain.State;

public interface ServiceUsageCapService {
    ServiceUsageCap getServiceUsageCap(final State state, final Service service);
}
