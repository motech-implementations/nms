package org.motechproject.nms.region.service.impl;

import org.motechproject.mds.query.QueryExecution;
import org.motechproject.mds.util.InstanceSecurityRestriction;
import org.motechproject.nms.region.domain.Circle;
import org.motechproject.nms.region.domain.LanguageLocation;
import org.motechproject.nms.region.domain.NationalDefaultLanguageLocation;
import org.motechproject.nms.region.repository.LanguageLocationDataService;
import org.motechproject.nms.region.repository.NationalDefaultLanguageLocationDataService;
import org.motechproject.nms.region.service.LanguageLocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.jdo.Query;
import java.util.ArrayList;
import java.util.List;

@Service("languageLocationService")
public class LanguageLocationServiceImpl implements LanguageLocationService {
    public static final int NATIONAL_DEFAULT_CODE = 0;
    private LanguageLocationDataService languageLocationDataService;
    private NationalDefaultLanguageLocationDataService nationalDefaultLanguageLocationDataService;

    @Autowired
    public LanguageLocationServiceImpl(LanguageLocationDataService languageLocationDataService,
                                       NationalDefaultLanguageLocationDataService nationalDefaultLanguageLocationDataService) {
        this.languageLocationDataService = languageLocationDataService;
        this.nationalDefaultLanguageLocationDataService = nationalDefaultLanguageLocationDataService;
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

    @Override
    public List<LanguageLocation> getAllForCircle(final Circle circle) {

        QueryExecution<List<LanguageLocation>> stateQueryExecution = new QueryExecution<List<LanguageLocation>>() {
            @Override
            public List<LanguageLocation> execute(Query query, InstanceSecurityRestriction restriction) {

                query.setFilter("circle == _circle");
                query.declareParameters("org.motechproject.nms.region.domain.Circle _circle");

                return (List<LanguageLocation>) query.execute(circle);
            }
        };

        List<LanguageLocation> languageLocations = languageLocationDataService.executeQuery(stateQueryExecution);

        if (languageLocations == null) {
            languageLocations = new ArrayList<>();
        }

        return languageLocations;
    }

    @Override
    public List<LanguageLocation> getAll() {
        List<LanguageLocation> languageLocations = languageLocationDataService.retrieveAll();

        if (languageLocations == null) {
            languageLocations = new ArrayList<>();
        }

        return languageLocations;
    }

    @Override
    public LanguageLocation getNationalDefaultLanguageLocation() {
        LanguageLocation languageLocation = null;

        NationalDefaultLanguageLocation nationalDefaultLanguageLocation;
        nationalDefaultLanguageLocation = nationalDefaultLanguageLocationDataService.findByCode(NATIONAL_DEFAULT_CODE);

        if (nationalDefaultLanguageLocation != null) {
            languageLocation = nationalDefaultLanguageLocation.getLanguageLocation();
        }

        return languageLocation;
    }
}
