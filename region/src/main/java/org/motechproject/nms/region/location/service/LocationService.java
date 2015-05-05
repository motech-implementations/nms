package org.motechproject.nms.region.location.service;

import org.motechproject.nms.region.language.domain.Language;
import org.motechproject.nms.region.location.domain.State;

/**
 * Service interfaces exposed by the language module
 */
public interface LocationService {
    State getStateForLanguage(Language language);
}
