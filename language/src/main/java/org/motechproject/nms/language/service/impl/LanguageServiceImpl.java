package org.motechproject.nms.language.service.impl;

import org.motechproject.nms.language.domain.CircleLanguage;
import org.motechproject.nms.language.repository.CircleLanguageDataService;
import org.motechproject.nms.language.service.LanguageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Simple implementation of the {@link LanguageService} interface.
 */
@Service("languageService")
public class LanguageServiceImpl implements LanguageService {

    @Autowired
    private CircleLanguageDataService circleLanguageDataService;

    @Override
    public Set<String> getCircleLanguages(String circle) {
        List<CircleLanguage> circleLanguages = circleLanguageDataService.findByCircle(circle);
        Set<String> languages = new HashSet<>();
        for (CircleLanguage circleLanguage : circleLanguages) {
            languages.add(circleLanguage.getLanguage());
        }
        return languages;
    }

}
