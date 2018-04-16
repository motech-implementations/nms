package org.motechproject.nms.region.service;

import org.motechproject.nms.region.domain.District;
import org.motechproject.nms.region.domain.HealthBlock;
import org.motechproject.nms.region.domain.HealthFacility;
import org.motechproject.nms.region.domain.HealthSubFacility;
import org.motechproject.nms.region.domain.State;
import org.motechproject.nms.region.domain.Taluka;
import org.motechproject.nms.region.domain.Village;
import org.motechproject.nms.region.exception.InvalidLocationException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.Map;

/**
 * Location service to get and validate location data
 */
public interface LocationService {

    /**
     * Get locations method that fetches the associated location code from the mapping
     * @param locationMapping mapping codes for location hierarchy
     * @return mapping of code to location object
     * @throws InvalidLocationException when the map of code set violates the location hierarchy
     */
    Map<String, Object> getLocations(Map<String, Object> locationMapping) throws InvalidLocationException;

    /**
     * Get locations method that fetches the associated location code from the mapping
     * @param locationMapping mapping codes for location hierarchy
     * @param createIfNotExist creates the location hierarchy if it doesnt exist already
     * @return mapping of code to location object
     * @throws InvalidLocationException when the map of code set violates the location hierarchy
     */
    Map<String, Object> getLocations(Map<String, Object> locationMapping, boolean createIfNotExist) throws InvalidLocationException;

    Taluka updateTaluka(Map<String, Object> locationMapping, Boolean createIfNotExists);

    HealthBlock updateBlock(Map<String, Object> locationMapping, Taluka taluka, Boolean createIfNotExists);

    HealthFacility updateFacility(Map<String, Object> locationMapping, HealthBlock healthBlock, Boolean createIfNotExists);

    HealthSubFacility updateSubFacility(Map<String, Object> locationMapping, HealthFacility healthFacility, Boolean createIfNotExists);

    Village updateVillage(Map<String, Object> locationMapping, Taluka taluka, Boolean createIfNotExists);

    State getState(Long stateId);

    District getDistrict(Long stateId, Long districtId);

    Taluka getTaluka(Long stateId, Long districtId, String talukaId);

    Village getVillage(Long stateId, Long districtId, String talukaId, Long vCode, Long svid);

    Village getCensusVillage(Long stateId, Long districtId, String talukaId, Long vCode);

    Village getNonCensusVillage(Long stateId, Long districtId, String talukaId, Long svid);

    HealthBlock getHealthBlock(Long stateId, Long districtId, String talukaId, Long healthBlockId);

    HealthFacility getHealthFacility(Long stateId, Long districtId, String talukaId, Long healthBlockId,
                                     Long healthFacilityId);

    HealthSubFacility getHealthSubFacility(Long stateId, Long districtId, String talukaId, Long healthBlockId,
                                           Long healthFacilityId, Long healthSubFacilityId);

    void updateLocations(Reader reader, List locationArrayList) throws IOException;

}
