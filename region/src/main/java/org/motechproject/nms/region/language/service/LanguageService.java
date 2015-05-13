package org.motechproject.nms.region.language.service;

import org.motechproject.nms.region.language.domain.Language;

import java.util.List;

/**
 * Service interfaces exposed by the language module
 */
public interface LanguageService {

    Language getLanguageByCode(String code);

    Language getDefaultCircleLanguage(String circle);

    List<Language> getCircleLanguages(String circle);

    Language getLanguage(String code);
}
