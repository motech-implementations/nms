package org.motechproject.nms.region.service;

import org.motechproject.nms.region.domain.Circle;
import org.motechproject.nms.region.domain.LanguageLocation;

import java.util.List;

public interface LanguageLocationService {
    List<LanguageLocation> getForCircle(final Circle circle);

    LanguageLocation getForCode(String code);
}
