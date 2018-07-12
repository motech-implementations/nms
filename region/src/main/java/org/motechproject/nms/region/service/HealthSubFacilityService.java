package org.motechproject.nms.region.service;

import org.motechproject.nms.region.domain.HealthFacility;
import org.motechproject.nms.region.domain.HealthSubFacility;
import org.motechproject.nms.region.domain.Taluka;

import javax.jdo.annotations.Transactional;
import java.util.List;
import java.util.Map;

public interface HealthSubFacilityService {
    HealthSubFacility findByHealthFacilityAndCode(HealthFacility healthFacility, Long code);
    HealthSubFacility create(HealthSubFacility healthSubFacility);
    HealthSubFacility update(HealthSubFacility healthSubFacility);

    Map<String, HealthSubFacility> fillHealthSubFacilityIds(List<Map<String, Object>> recordList, Map<String, HealthFacility> healthFacilityHashMap);

    Long createUpdateHealthSubFacilities(List<Map<String, Object>> recordList, Map<String, Taluka> talukaHashMap, Map<String, HealthFacility> healthFacilityHashMap);

    @Transactional
    Long createUpdateVillageHealthSubFacility(List<Map<String, Object>> recordList);
}
