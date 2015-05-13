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

    @Override
    public LanguageLocation getDefaultForCircle(final Circle circle) {

        QueryExecution<LanguageLocation> stateQueryExecution = new QueryExecution<LanguageLocation>() {
            @Override
            public LanguageLocation execute(Query query, InstanceSecurityRestriction restriction) {

                query.setFilter("circle == _circle && defaultForCircle == true");
                query.declareParameters("org.motechproject.nms.region.domain.Circle _circle");
                query.setUnique(true);

                return (LanguageLocation) query.execute(circle);
            }
        };

        LanguageLocation stateServiceUsageCap = languageLocationDataService.executeQuery(stateQueryExecution);

        if (null != stateServiceUsageCap) {
            return stateServiceUsageCap;
        }

        return null;
    }
}
