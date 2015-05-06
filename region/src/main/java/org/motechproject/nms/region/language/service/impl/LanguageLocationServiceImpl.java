package org.motechproject.nms.region.language.service.impl;

import org.motechproject.mds.query.QueryExecution;
import org.motechproject.mds.util.InstanceSecurityRestriction;
import org.motechproject.nms.region.circle.domain.Circle;
import org.motechproject.nms.region.language.domain.LanguageLocation;
import org.motechproject.nms.region.language.repository.LanguageLocationDataService;
import org.motechproject.nms.region.language.service.LanguageLocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.jdo.Query;
import java.util.ArrayList;
import java.util.List;

@Service("languageLocationService")
public class LanguageLocationServiceImpl implements LanguageLocationService{
    private LanguageLocationDataService languageLocationDataService;

    @Autowired
    public LanguageLocationServiceImpl(LanguageLocationDataService languageLocationDataService) {
        this.languageLocationDataService = languageLocationDataService;
    }

    /**
     * Returns the default languagelocation for the circle
     *
     * @param circle The code for the circle
     * @return The default languagelocation for the circle or null if no languageslocations are found for circle
     */
    @Override
    public LanguageLocation getDefaultLanguageLocationForCircle(final Circle circle) {
        LanguageLocation languageLocation = null;

        QueryExecution<LanguageLocation> stateQueryExecution = new QueryExecution<LanguageLocation>() {
            @Override
            public LanguageLocation execute(Query query, InstanceSecurityRestriction restriction) {

                query.setFilter("circle == circle_param && isDefaultLanguage == true");
                query.declareParameters("org.motechproject.nms.region.circle.domain.Circle circle_param");
                query.setUnique(true);

                return (LanguageLocation) query.execute(circle);
            }
        };

        languageLocation = languageLocationDataService.executeQuery(stateQueryExecution);

        return languageLocation;
    }

    /**
     * Returns all languagelocations for a given circle
     *
     * @param circle The code for the circle
     * @return A list that contains all languagelocations for the circle.  If no languagelocations are found
     * an empty list is returned
     */
    @Override
    public List<LanguageLocation> getLanguageLocationsForCircle(final Circle circle) {
        List<LanguageLocation> languageLocation = new ArrayList<>();

        QueryExecution<List<LanguageLocation>> stateQueryExecution = new QueryExecution<List<LanguageLocation>>() {
            @Override
            public List<LanguageLocation> execute(Query query, InstanceSecurityRestriction restriction) {

                query.setFilter("circle == circle_param");
                query.declareParameters("org.motechproject.nms.region.circle.domain.Circle circle_param");

                return (List<LanguageLocation>) query.execute(circle);
            }
        };

        languageLocation = languageLocationDataService.executeQuery(stateQueryExecution);

        return languageLocation;
    }
}
