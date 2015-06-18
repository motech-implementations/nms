package org.motechproject.nms.region.service.impl;

import org.motechproject.nms.region.domain.District;
import org.motechproject.nms.region.domain.HealthBlock;
import org.motechproject.nms.region.domain.HealthFacility;
import org.motechproject.nms.region.domain.HealthSubFacility;
import org.motechproject.nms.region.domain.State;
import org.motechproject.nms.region.domain.Taluka;
import org.motechproject.nms.region.domain.Village;
import org.motechproject.nms.region.repository.DistrictDataService;
import org.motechproject.nms.region.repository.HealthBlockDataService;
import org.motechproject.nms.region.repository.HealthFacilityDataService;
import org.motechproject.nms.region.repository.HealthSubFacilityDataService;
import org.motechproject.nms.region.repository.StateDataService;
import org.motechproject.nms.region.repository.TalukaDataService;
import org.motechproject.nms.region.repository.VillageDataService;
import org.motechproject.nms.region.service.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Location service impl to get location objects
 */
@Service("locationService")
public class LocationServiceImpl implements LocationService {

    private StateDataService stateDataService;

    private DistrictDataService districtDataService;

    private TalukaDataService talukaDataService;

    private VillageDataService villageDataService;

    private HealthBlockDataService healthBlockDataService;

    private HealthFacilityDataService healthFacilityDataService;

    private HealthSubFacilityDataService healthSubFacilityDataService;

    @Autowired
    public LocationServiceImpl(StateDataService stateDataService, DistrictDataService districtDataService,
                               TalukaDataService talukaDataService, VillageDataService villageDataService,
                               HealthBlockDataService healthBlockDataService,
                               HealthFacilityDataService healthFacilityDataService,
                               HealthSubFacilityDataService healthSubFacilityDataService) {
        this.stateDataService = stateDataService;
        this.districtDataService = districtDataService;
        this.talukaDataService = talukaDataService;
        this.villageDataService = villageDataService;
        this.healthBlockDataService = healthBlockDataService;
        this.healthFacilityDataService = healthFacilityDataService;
        this.healthSubFacilityDataService = healthSubFacilityDataService;
    }

    @Override
    public State getState(Long stateId) {

        return stateDataService.findByCode(stateId);
    }

    @Override
    public District getDistrict(Long stateId, Long districtId) {

        State state = getState(stateId);

        return null;
    }

    @Override
    public Taluka getTaluka(Long stateId, Long districtId, Long talukaId) {
        return null;
    }

    @Override
    public Village getCensusVillage(Long stateId, Long DistrictId, Long talukaId, Long vCode) {
        return null;
    }

    @Override
    public Village getNonCensusVillage(Long stateId, Long districtId, Long talukaId, Long svid) {
        return null;
    }

    @Override
    public HealthBlock getHealthBlock(Long stateId, Long districtId, Long talukaId, Long healthBlockId) {
        return null;
    }

    @Override
    public HealthFacility getHealthFacility(Long stateId, Long districtId, Long talukaId, Long healthBlockId, Long healthFacilityId) {
        return null;
    }

    @Override
    public HealthSubFacility getHealthSubFacility(Long stateId, Long districtId, Long talukaId, Long healthBlockId, Long healthFacilityId, Long healthSubFacilityId) {
        return null;
    }
}
