package org.motechproject.nms.region.service.impl;

import org.motechproject.nms.region.domain.Circle;
import org.motechproject.nms.region.domain.District;
import org.motechproject.nms.region.domain.Language;
import org.motechproject.nms.region.domain.NationalDefaultLanguage;
import org.motechproject.nms.region.domain.State;
import org.motechproject.nms.region.repository.LanguageDataService;
import org.motechproject.nms.region.repository.NationalDefaultLanguageDataService;
import org.motechproject.nms.region.service.DistrictService;
import org.motechproject.nms.region.service.LanguageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service("languageService")
public class LanguageServiceImpl implements LanguageService {
    public static final int NATIONAL_DEFAULT_CODE = 0;
    private LanguageDataService languageDataService;
    private NationalDefaultLanguageDataService nationalDefaultLanguageDataService;
    private DistrictService districtService;

    @Autowired
    public LanguageServiceImpl(LanguageDataService languageDataService,
                               NationalDefaultLanguageDataService nationalDefaultLanguageDataService,
                               DistrictService districtService) {
        this.languageDataService = languageDataService;
        this.nationalDefaultLanguageDataService = nationalDefaultLanguageDataService;
        this.districtService = districtService;
    }

    /**
     * Given a languageCode returns the object if one exists
     * @param code the code for the language record
     * @return The language Record if it exists
     */
    @Override
    public Language getForCode(String code) {
        return languageDataService.findByCode(code);
    }


    @Override
    public List<Language> getAllForCircle(final Circle circle) {

        List<Language> languages = new ArrayList<>();

        for (State state : circle.getStates()) {
            for (District district : state.getDistricts()) {
                languages.add(district.getLanguage());
            }
        }

        return languages;
    }


    @Override
    public List<Language> getAll() {
        List<Language> languages = languageDataService.retrieveAll();

        if (languages == null) {
            languages = new ArrayList<>();
        }

        return languages;
    }

    @Override
    public Language getNationalDefaultLanguage() {
        Language language = null;

        NationalDefaultLanguage nationalDefaultLanguage;
        nationalDefaultLanguage = nationalDefaultLanguageDataService.findByCode(NATIONAL_DEFAULT_CODE);

        if (nationalDefaultLanguage != null) {
            language = nationalDefaultLanguage.getLanguage();
        }

        return language;
    }

    @Override
    public Set<State> getAllStatesForLanguage(Language language) {
        Set<State> states = new HashSet<>();
        Set<District> districts = districtService.getAllForLanguage(language);

        for (District district: districts) {
            states.add(district.getState());
        }

        return states;
    }
}
