package org.motechproject.nms.region.service;

import org.motechproject.nms.region.domain.Circle;
import org.motechproject.nms.region.domain.State;

import java.util.List;
import java.util.Set;

public interface CircleService {
    Circle getByName(String name);
    List<Circle> getAll();
    Set<Circle> getAllInState(final State state);
    boolean circleNameExists(String circleName);
}
