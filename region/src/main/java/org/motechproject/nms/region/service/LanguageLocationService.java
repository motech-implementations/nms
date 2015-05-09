package org.motechproject.nms.region.service;

import org.motechproject.nms.region.domain.LanguageLocation;

public interface LanguageLocationService {
    LanguageLocation getForCode(String code);
}
