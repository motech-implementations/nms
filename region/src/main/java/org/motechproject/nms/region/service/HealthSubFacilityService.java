package org.motechproject.nms.region.service;

import org.motechproject.nms.region.domain.*;

import java.util.List;
import java.util.Map;

public interface HealthSubFacilityService {
    HealthSubFacility findByHealthFacilityAndCode(HealthFacility healthFacility, Long code);
    HealthSubFacility findByStateAndCode(State state, Long code);
    HealthSubFacility create(HealthSubFacility healthSubFacility);
    HealthSubFacility update(HealthSubFacility healthSubFacility);

    Long createUpdateHealthSubFacilities(List<Map<String, Object>> recordList, Map<String, State> stateHashMap, Map<String, District> districtHashMap, Map<String, Taluka> talukaHashMap, Map<String, HealthFacility> healthFacilityHashMap);

    Long createUpdateVillageHealthSubFacility(List<Map<String, Object>> recordList);
}
