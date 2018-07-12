package org.motechproject.nms.region.service;

import org.motechproject.nms.region.domain.HealthBlock;
import org.motechproject.nms.region.domain.HealthFacility;
import org.motechproject.nms.region.domain.Taluka;

import java.util.List;
import java.util.Map;

public interface HealthFacilityService {
    HealthFacility findByHealthBlockAndCode(HealthBlock healthBlock, Long code);
    HealthFacility create(HealthFacility healthFacility);
    HealthFacility update(HealthFacility healthFacility);

    Map<String, HealthFacility> fillHealthFacilityIds(List<Map<String, Object>> recordList, Map<String, HealthBlock> healthBlockHashMap);

    Long createUpdateHealthFacilities(List<Map<String, Object>> recordList, Map<String, Taluka> talukaHashMap, Map<String, HealthBlock> healthBlockHashMap);

    Map<String, HealthFacility> fillHealthFacilitiesFromTalukas(List<Map<String, Object>> recordList, Map<String, Taluka> talukaHashMap);
}
