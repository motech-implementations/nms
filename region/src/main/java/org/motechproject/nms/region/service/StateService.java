package org.motechproject.nms.region.service;

import org.motechproject.nms.region.domain.State;

public interface StateService {
    State findByName(String name);
    State findByCode(Long code);
}
