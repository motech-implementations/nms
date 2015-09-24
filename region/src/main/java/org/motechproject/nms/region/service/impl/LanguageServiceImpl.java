package org.motechproject.nms.region.service.impl;

import com.google.common.collect.Sets;
import org.motechproject.event.MotechEvent;
import org.motechproject.event.listener.EventRelay;
import org.motechproject.event.listener.annotations.MotechListener;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.jdo.Query;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service("languageService")
public class LanguageServiceImpl implements LanguageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LanguageServiceImpl.class);

    public static final int NATIONAL_DEFAULT_CODE = 0;
    public static final String LANGUAGE_CACHE_EVICT_MESSAGE = "nms.region.cache.evict.language";

    private LanguageDataService languageDataService;
    private NationalDefaultLanguageDataService nationalDefaultLanguageDataService;
    private DistrictService districtService;
    private EventRelay eventRelay;


    @Autowired
    public LanguageServiceImpl(LanguageDataService languageDataService,
                               NationalDefaultLanguageDataService nationalDefaultLanguageDataService,
                               DistrictService districtService, EventRelay eventRelay) {
        this.languageDataService = languageDataService;
        this.nationalDefaultLanguageDataService = nationalDefaultLanguageDataService;
        this.districtService = districtService;
        this.eventRelay = eventRelay;
    }


    /**
     * Given a languageCode returns the object if one exists
     * @param code the code for the language record
     * @return The language Record if it exists
     */
    @Override
    @Cacheable(value = "language", key = "#p0.concat('-0')")
    public Language getForCode(String code) {
        return languageDataService.findByCode(code);
    }


    /**
     * Given a language name returns the object if one exists
     * @param name the language name
     * @return The language Record if it exists
     */
    @Override
    @Cacheable(value = "language", key = "'0-'.concat(#p0)")
    public Language getForName(String name) {
        return languageDataService.findByName(name);
    }


    @Override
    @Cacheable(value = "languages", key = "#p0")
    public Set<Language> getAllForCircle(final Circle circle) {

        SqlQueryExecution<List<Language>> queryExecution = new SqlQueryExecution<List<Language>>() {

            @Override
            public String getSqlQuery() {
                String query = "select * " +
                        "from nms_languages l " +
                        "join nms_districts d on  d.language_id_oid = l.id " +
                        "join nms_states s on d.state_id_oid = s.id " +
                        "join nms_states_join_circles sxc on s.id = sxc.state_id and sxc.circle_id = ?";

                return query;
            }

            @Override
            public List<Language> execute(Query query) {
                query.setClass(Language.class);
                return (List<Language>) query.execute(circle.getId());
            }
        };

        Set<Language> languages = Sets.newHashSet(languageDataService.executeSQLQuery(queryExecution));

        if (languages == null) {
            languages = new HashSet<>();
        }

        return languages;
    }


    @Override
    @Cacheable(value = "languages", key = "'all-languages'")
    public Set<Language> getAll() {
        Set<Language> languages = new HashSet<>(languageDataService.retrieveAll());

        if (languages == null) {
            languages = new HashSet<>();
        }

        return languages;
    }


    @Override
    @Cacheable(value = "language", key = "'national-default'")
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


    @CacheEvict(value = {"language", "languages" }, allEntries = true)
    public void broadcastCacheEvictMessage(Language language) {
        MotechEvent motechEvent = new MotechEvent(LANGUAGE_CACHE_EVICT_MESSAGE);
        LOGGER.debug("*** BROADCAST CACHE EVICT MSG ***");
        eventRelay.sendEventMessage(motechEvent);
    }


    @MotechListener(subjects = { LANGUAGE_CACHE_EVICT_MESSAGE })
    @CacheEvict(value = {"language", "languages" }, allEntries = true)
    public void cacheEvict(MotechEvent event) {
        LOGGER.debug("*** RECEIVE CACHE EVICT MSG ***");
    }

}
