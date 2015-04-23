package org.motechproject.nms.language.service;

import org.motechproject.nms.language.domain.Language;
import java.util.List;

/**
 * Service interfaces exposed by the language module
 */
public interface LanguageService {

    Language getLanguageByCode(Integer code);

    Language getDefaultCircleLanguage(String circle);

    List<Language> getCircleLanguages(String circle);

    Language getLanguage(int code);
}
