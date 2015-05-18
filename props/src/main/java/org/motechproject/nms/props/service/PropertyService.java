package org.motechproject.nms.props.service;

import org.motechproject.nms.props.domain.Service;
import org.motechproject.nms.region.domain.State;

public interface PropertyService {
    boolean isServiceDeployedInState(Service service, State state);
}
