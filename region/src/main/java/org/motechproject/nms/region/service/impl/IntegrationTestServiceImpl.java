package org.motechproject.nms.region.service.impl;

import org.motechproject.nms.region.repository.CircleDataService;
import org.motechproject.nms.region.repository.DistrictDataService;
import org.motechproject.nms.region.repository.HealthBlockDataService;
import org.motechproject.nms.region.repository.HealthFacilityDataService;
import org.motechproject.nms.region.repository.HealthFacilityTypeDataService;
import org.motechproject.nms.region.repository.HealthSubFacilityDataService;
import org.motechproject.nms.region.repository.LanguageDataService;
import org.motechproject.nms.region.repository.LanguageLocationDataService;
import org.motechproject.nms.region.repository.NationalDefaultLanguageLocationDataService;
import org.motechproject.nms.region.repository.StateDataService;
import org.motechproject.nms.region.repository.TalukaDataService;
import org.motechproject.nms.region.repository.VillageDataService;
import org.motechproject.nms.region.service.IntegrationTestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("itService")
public class IntegrationTestServiceImpl implements IntegrationTestService {

    @Autowired
    private CircleDataService circleDataService;
    @Autowired
    private DistrictDataService districtDataService;
    @Autowired
    private HealthBlockDataService healthBlockDataService;
    @Autowired
    private HealthFacilityDataService healthFacilityDataService;
    @Autowired
    private HealthFacilityTypeDataService healthFacilityTypeDataService;
    @Autowired
    private HealthSubFacilityDataService healthSubFacilityDataService;
    @Autowired
    private LanguageLocationDataService languageLocationDataService;
    @Autowired
    private NationalDefaultLanguageLocationDataService nationalDefaultLanguageLocationDataService;
    @Autowired
    private StateDataService stateDataService;
    @Autowired
    private TalukaDataService talukaDataService;
    @Autowired
    private VillageDataService villageDataService;
    @Autowired
    private LanguageDataService languageDataService;


    public void deleteAll() {
        circleDataService.deleteAll();
        districtDataService.deleteAll();
        healthBlockDataService.deleteAll();
        healthFacilityDataService.deleteAll();
        healthFacilityTypeDataService.deleteAll();
        healthSubFacilityDataService.deleteAll();
        languageLocationDataService.deleteAll();
        languageDataService.deleteAll();
        nationalDefaultLanguageLocationDataService.deleteAll();
        stateDataService.deleteAll();
        talukaDataService.deleteAll();
        villageDataService.deleteAll();
    }
}
