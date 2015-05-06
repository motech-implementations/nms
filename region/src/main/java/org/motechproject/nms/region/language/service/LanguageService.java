package org.motechproject.nms.region.language.service;

import org.motechproject.nms.region.language.domain.Language;

/**
 * Service interfaces exposed by the language module
 */
public interface LanguageService {

    Language getLanguageByCode(String code);

    Language getLanguage(String code);
}
