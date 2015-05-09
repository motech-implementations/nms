package org.motechproject.nms.region.service.impl;

import org.motechproject.nms.region.domain.LanguageLocation;
import org.motechproject.nms.region.repository.LanguageLocationDataService;
import org.motechproject.nms.region.service.LanguageLocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("languageLocationService")
public class LanguageLocationServiceImpl implements LanguageLocationService {
    private LanguageLocationDataService languageLocationDataService;

    @Autowired
    public LanguageLocationServiceImpl(LanguageLocationDataService languageLocationDataService) {
        this.languageLocationDataService = languageLocationDataService;
    }

    /**
     * Given a languageLocationCode returns the object if one exists
     * @param code the code for the language location record
     * @return The languageLocationRecord if it exists
     */
    @Override
    public LanguageLocation getForCode(String code) {
        return languageLocationDataService.findByCode(code);
    }
}
