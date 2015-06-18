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
import org.motechproject.nms.region.service.DistrictService;
import org.motechproject.nms.region.service.HealthBlockService;
import org.motechproject.nms.region.service.HealthFacilityService;
import org.motechproject.nms.region.service.HealthSubFacilityService;
import org.motechproject.nms.region.service.LocationService;
import org.motechproject.nms.region.service.TalukaService;
import org.motechproject.nms.region.service.VillageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Location service impl to get location objects
 */
@Service("locationService")
public class LocationServiceImpl implements LocationService {

    private StateDataService stateDataService;

    private DistrictService districtService;

    private TalukaService talukaService;

    private VillageService villageService;

    private HealthBlockService healthBlockService;

    private HealthFacilityService healthFacilityService;

    private HealthSubFacilityService healthSubFacilityService;

    @Autowired
    public LocationServiceImpl(StateDataService stateDataService, DistrictService districtService,
                               TalukaService talukaService, VillageService villageService,
                               HealthBlockService healthBlockService, HealthFacilityService healthFacilityService,
                               HealthSubFacilityService healthSubFacilityService) {
        this.stateDataService = stateDataService;
        this.districtService = districtService;
        this.talukaService = talukaService;
        this.villageService = villageService;
        this.healthBlockService = healthBlockService;
        this.healthFacilityService = healthFacilityService;
        this.healthSubFacilityService = healthSubFacilityService;
    }

    @Override
    public State getState(Long stateId) {

        return stateDataService.findByCode(stateId);
    }

    @Override
    public District getDistrict(Long stateId, Long districtId) {

        State state = getState(stateId);

        if (state != null) {
            return districtService.findByStateAndCode(state, districtId);
        }

        return null;
    }

    @Override
    public Taluka getTaluka(Long stateId, Long districtId, String talukaId) {

        District district = getDistrict(stateId, districtId);

        if (district != null) {
            return talukaService.findByDistrictAndCode(district, talukaId);
        }

        return null;
    }

    @Override
    public Village getCensusVillage(Long stateId, Long districtId, Long talukaId, Long vCode) {


    }

    @Override
    public Village getNonCensusVillage(Long stateId, Long districtId, Long talukaId, Long svid) {

        Taluka taluka = getTaluka(stateId, districtId, talukaId);

        if (taluka != null) {
            return villageService.findByTalukaAndSvid(taluka, svid);
        }

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
