package org.motechproject.nms.region.service;

import org.motechproject.nms.region.domain.District;
import org.motechproject.nms.region.domain.State;
import org.motechproject.nms.region.domain.Taluka;

import java.util.List;
import java.util.Map;

public interface TalukaService {
    Taluka findByDistrictAndCode(District district, String code);
    Taluka create(Taluka taluka);
    Taluka update(Taluka taluka);

    Map<String, Taluka> fillTalukaIds(List<Map<String, Object>> recordList, Map<String, District> districtHashMap);

    Long createUpdateTalukas(List<Map<String, Object>> recordList, Map<String, State> stateHashMap, Map<String, District> districtHashMap);
}
