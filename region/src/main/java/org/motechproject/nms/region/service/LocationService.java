package org.motechproject.nms.region.service;

import org.motechproject.nms.region.domain.District;
import org.motechproject.nms.region.domain.HealthBlock;
import org.motechproject.nms.region.domain.HealthFacility;
import org.motechproject.nms.region.domain.HealthSubFacility;
import org.motechproject.nms.region.domain.State;
import org.motechproject.nms.region.domain.Taluka;
import org.motechproject.nms.region.domain.Village;
import org.motechproject.nms.region.exception.InvalidLocationException;

import java.util.Map;

/**
 * Location service to get and validation location data
 */
public interface LocationService {

    Map<String, Object> getLocations(Map<String, Object> locationMapping) throws InvalidLocationException;

    State getState(Long stateId);

    District getDistrict(Long stateId, Long districtId);

    Taluka getTaluka(Long stateId, Long districtId, String talukaId);

    Village getCensusVillage(Long stateId, Long DistrictId, String talukaId, Long vCode);

    Village getNonCensusVillage(Long stateId, Long districtId, String talukaId, Long svid);

    HealthBlock getHealthBlock(Long stateId, Long districtId, String talukaId, Long healthBlockId);

    HealthFacility getHealthFacility(Long stateId, Long districtId, String talukaId, Long healthBlockId,
                                     Long healthFacilityId);

    HealthSubFacility getHealthSubFacility(Long stateId, Long districtId, String talukaId, Long healthBlockId,
                                           Long healthFacilityId, Long healthSubFacilityId);

}
