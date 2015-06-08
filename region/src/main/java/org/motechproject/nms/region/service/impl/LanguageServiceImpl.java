package org.motechproject.nms.region.service.impl;

import org.motechproject.mds.query.SqlQueryExecution;
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

import javax.jdo.Query;
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

        SqlQueryExecution<List<Language>> queryExecution = new SqlQueryExecution<List<Language>>() {

            @Override
            public String getSqlQuery() {
                String query = "select * " +
                        "from nms_languages l " +
                        "join nms_districts d on  d.language_id_oid = l.id " +
                        "join nms_states s on d.state_id_oid = s.id " +
                        "join state_circles cxs on s.id = cxs.state_id and cxs.circle_id = ?";

                return query;
            }

            @Override
            public List<Language> execute(Query query) {
                query.setClass(Language.class);
                return (List<Language>) query.execute(circle.getId());
            }
        };

        List<Language> languages;
        try {
            languages = languageDataService.executeSQLQuery(queryExecution);
        } catch (Exception e) {
            return null;
        }

        if (languages == null) {
            languages = new ArrayList<>();
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
