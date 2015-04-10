package org.motechproject.nms.language.service.impl;

import org.motechproject.nms.language.domain.CircleLanguage;
import org.motechproject.nms.language.domain.Language;
import org.motechproject.nms.language.repository.CircleLanguageDataService;
import org.motechproject.nms.language.service.LanguageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the {@link LanguageService} interface.
 */
@Service("languageService")
public class LanguageServiceImpl implements LanguageService {

    @Autowired
    private CircleLanguageDataService circleLanguageDataService;

    @Override
    public List<Language> getCircleLanguages(String circle) {
        List<CircleLanguage> circleLanguages =  circleLanguageDataService.findByCircle(circle);
        List<Language> languages = new ArrayList<Language>();

        for (CircleLanguage circleLanguage : circleLanguages) {
            languages.add(circleLanguage.getLanguage());
        }

        return languages;
    }

}
