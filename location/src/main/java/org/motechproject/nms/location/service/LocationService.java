package org.motechproject.nms.location.service;

import org.motechproject.nms.language.domain.Language;
import org.motechproject.nms.location.domain.State;

/**
 * Service interfaces exposed by the language module
 */
public interface LocationService {
    State getStateForLanguage(Language language);
}
