package org.motechproject.nms.region.service;

import org.motechproject.nms.region.domain.*;

import java.util.List;
import java.util.Map;

public interface HealthFacilityService {
    HealthFacility findByHealthBlockAndCode(HealthBlock healthBlock, Long code);
    HealthFacility create(HealthFacility healthFacility);
    HealthFacility update(HealthFacility healthFacility);

    Long createUpdateHealthFacilities(List<Map<String, Object>> recordList, Map<String, State> stateHashMap, Map<String, District> districtHashMap, Map<String, Taluka> talukaHashMap, Map<String, HealthBlock> healthBlockHashMap);

    Map<String, HealthFacility> fillHealthFacilitiesFromTalukas(List<Map<String, Object>> recordList, Map<String, Taluka> talukaHashMap);
}
