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

    /**
     * Returns the default language for the circle
     *
     * @param circle The code for the circle
     * @return The default language for the circle or null if no languages are found for circle
     */
    @Override
    public Language getDefaultCircleLanguage(String circle) {
        Language defaultLanguage = null;
        List<CircleLanguage> circleLanguages = circleLanguageDataService.findByCircle(circle);

        //todo #58 revisit when we know what to do with multiple default languages
        if (circleLanguages.size() > 0) {
            defaultLanguage = circleLanguages.get(0).getLanguage();
        }

        return defaultLanguage;
    }

    /**
     * Returns all languages for a given circle
     *
     * @param circle The code for the circle
     * @return A list that contains all languages for the circle.  If no languages are found an empty list is returned
     */
    @Override
    public List<Language> getCircleLanguages(String circle) {
        List<CircleLanguage> circleLanguages = circleLanguageDataService.findByCircle(circle);
        List<Language> languages = new ArrayList<Language>();

        for (CircleLanguage circleLanguage : circleLanguages) {
            languages.add(circleLanguage.getLanguage());
        }

        return languages;
    }

}
