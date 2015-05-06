package org.motechproject.nms.region.language.service;

import org.motechproject.nms.region.circle.domain.Circle;
import org.motechproject.nms.region.language.domain.LanguageLocation;

import java.util.List;

public interface LanguageLocationService {
    LanguageLocation getDefaultLanguageLocationForCircle(final Circle circle);

    List<LanguageLocation> getLanguageLocationsForCircle(final Circle circle);
}
