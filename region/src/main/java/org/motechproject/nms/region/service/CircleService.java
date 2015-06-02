package org.motechproject.nms.region.service;

import org.motechproject.nms.region.domain.Circle;

import java.util.List;

public interface CircleService {
    Circle getByName(String name);
    List<Circle> getAll();
}
