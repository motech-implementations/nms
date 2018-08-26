package org.motechproject.nms.region.service;

import org.motechproject.nms.region.domain.District;
import org.motechproject.nms.region.domain.HealthBlock;
import org.motechproject.nms.region.domain.State;
import org.motechproject.nms.region.domain.Taluka;

import java.util.List;
import java.util.Map;

public interface HealthBlockService {
    HealthBlock findByTalukaAndCode(Taluka taluka, Long code);

    HealthBlock findByDistrictAndCode(District district, Long code);

    HealthBlock create(HealthBlock healthBlock);
    HealthBlock update(HealthBlock healthBlock);

    Map<String, HealthBlock> fillHealthBlockIds(List<Map<String, Object>> recordList, Map<String, District> districtHashMap);

    Long createUpdateHealthBlocks(List<Map<String, Object>> recordList, Map<String, State> stateHashMap, Map<String, District> districtHashMap, Map<String, Taluka> talukaHashMap);

    Long createUpdateTalukaHealthBlock(List<Map<String, Object>> recordList);
}
