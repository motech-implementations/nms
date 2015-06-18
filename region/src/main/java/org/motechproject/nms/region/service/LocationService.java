package org.motechproject.nms.region.service;

import org.motechproject.nms.region.domain.District;
import org.motechproject.nms.region.domain.HealthBlock;
import org.motechproject.nms.region.domain.HealthFacility;
import org.motechproject.nms.region.domain.HealthSubFacility;
import org.motechproject.nms.region.domain.State;
import org.motechproject.nms.region.domain.Taluka;
import org.motechproject.nms.region.domain.Village;

/**
 * Location service to get and validation location data
 */
public interface LocationService {

    State getState(Long stateId);

    District getDistrict(Long stateId, Long districtId);

    Taluka getTaluka(Long stateId, Long districtId, Long talukaId);

    Village getCensusVillage(Long stateId, Long DistrictId, Long talukaId, Long vCode);

    Village getNonCensusVillage(Long stateId, Long districtId, Long talukaId, Long svid);

    HealthBlock getHealthBlock(Long stateId, Long districtId, Long talukaId, Long healthBlockId);

    HealthFacility getHealthFacility(Long stateId, Long districtId, Long talukaId, Long healthBlockId,
                                     Long healthFacilityId);

    HealthSubFacility getHealthSubFacility(Long stateId, Long districtId, Long talukaId, Long healthBlockId,
                                           Long healthFacilityId, Long healthSubFacilityId);

}
