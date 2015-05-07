package org.motechproject.nms.region.service.impl;

import org.motechproject.mds.query.QueryExecution;
import org.motechproject.mds.util.InstanceSecurityRestriction;
import org.motechproject.nms.region.domain.Circle;
import org.motechproject.nms.region.domain.LanguageLocation;
import org.motechproject.nms.region.repository.LanguageLocationDataService;
import org.motechproject.nms.region.service.LanguageLocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.jdo.Query;
import java.util.ArrayList;
import java.util.List;

@Service("languageLocationService")
public class LanguageLocationServiceImpl implements LanguageLocationService {
    private LanguageLocationDataService languageLocationDataService;

    @Autowired
    public LanguageLocationServiceImpl(LanguageLocationDataService languageLocationDataService) {
        this.languageLocationDataService = languageLocationDataService;
    }

    /**
     * Returns all languagelocations for a given circle
     *
     * @param circle The code for the circle
     * @return A list that contains all languagelocations for the circle.  If no languagelocations are found
     * an empty list is returned
     */
    @Override
    public List<LanguageLocation> getForCircle(final Circle circle) {
        List<LanguageLocation> languageLocation = new ArrayList<>();

        QueryExecution<List<LanguageLocation>> stateQueryExecution = new QueryExecution<List<LanguageLocation>>() {
            @Override
            public List<LanguageLocation> execute(Query query, InstanceSecurityRestriction restriction) {

                query.setFilter("circle == circle_param");
                query.declareParameters("org.motechproject.nms.region.domain.Circle circle_param");

                return (List<LanguageLocation>) query.execute(circle);
            }
        };

        languageLocation = languageLocationDataService.executeQuery(stateQueryExecution);

        return languageLocation;
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
