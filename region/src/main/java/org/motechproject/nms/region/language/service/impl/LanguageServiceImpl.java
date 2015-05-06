package org.motechproject.nms.region.language.service.impl;

import org.motechproject.nms.region.language.domain.Language;
import org.motechproject.nms.region.language.repository.LanguageDataService;
import org.motechproject.nms.region.language.service.LanguageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementation of the {@link LanguageService} interface.
 */
@Service("languageService")
public class LanguageServiceImpl implements LanguageService {

    @Autowired
    private LanguageDataService languageDataService;

    /**
     * Returns the language for a given code
     * @param code the language code
     * @return the language object if found
     */
    public Language getLanguageByCode(String code) {
        return languageDataService.findByCode(code);
    }

    @Override
    public Language getLanguage(String code) {
        return languageDataService.findByCode(code);
    }

}
