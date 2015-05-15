package org.motechproject.nms.region.service;

import org.motechproject.nms.region.domain.Circle;
import org.motechproject.nms.region.domain.LanguageLocation;

import java.util.List;

public interface LanguageLocationService {
    LanguageLocation getForCode(String code);

    LanguageLocation getDefaultForCircle(Circle circle);

    List<LanguageLocation> getAllForCircle(Circle circle);

    List<LanguageLocation> getAll();

    LanguageLocation getNationalDefaultLanguageLocation();
}
