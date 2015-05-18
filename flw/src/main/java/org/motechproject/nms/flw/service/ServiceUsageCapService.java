package org.motechproject.nms.flw.service;

import org.motechproject.nms.flw.domain.ServiceUsageCap;
import org.motechproject.nms.props.domain.Service;
import org.motechproject.nms.region.domain.State;

public interface ServiceUsageCapService {
    ServiceUsageCap getServiceUsageCap(final State state, final Service service);
}
