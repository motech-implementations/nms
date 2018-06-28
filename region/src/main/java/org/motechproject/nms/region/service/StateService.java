package org.motechproject.nms.region.service;

import org.motechproject.nms.region.domain.Circle;
import org.motechproject.nms.region.domain.State;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface StateService {
    Set<State> getAllInCircle(final Circle circle);

    Map<String, State> fillStateIds(List<Map<String, Object>> recordList);
}
