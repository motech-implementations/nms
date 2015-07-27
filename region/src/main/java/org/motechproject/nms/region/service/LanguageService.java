package org.motechproject.nms.region.service;

import org.motechproject.nms.region.domain.Circle;
import org.motechproject.nms.region.domain.Language;
import org.motechproject.nms.region.domain.State;

import java.util.Set;

public interface LanguageService {
    Language getForCode(String code);

    Set<Language> getAllForCircle(Circle circle);

    Set<Language> getAll();

    Language getNationalDefaultLanguage();

    Set<State> getAllStatesForLanguage(Language language);
}
