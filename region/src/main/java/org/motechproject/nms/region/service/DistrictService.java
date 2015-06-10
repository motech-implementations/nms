package org.motechproject.nms.region.service;

import org.motechproject.nms.region.domain.District;
import org.motechproject.nms.region.domain.Language;

import java.util.Set;

public interface DistrictService {
    Set<District> getAllForLanguage(Language language);
}
