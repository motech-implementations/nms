package org.motechproject.nms.region.service;

import org.motechproject.nms.region.domain.District;
import org.motechproject.nms.region.domain.State;
import org.motechproject.nms.region.domain.Taluka;
import org.motechproject.nms.region.domain.Village;

import java.util.List;
import java.util.Map;

public interface VillageService {
    Village findByTalukaAndVcodeAndSvid(Taluka taluka, long vcode, long svid);
    Village create(Village village);
    Village update(Village village);
    Village findByStateAndVcodeAndSvid(State state, long vcode, long svid);

    Long createUpdateVillages(List<Map<String, Object>> recordList, Map<String, State> stateHashMap, Map<String, District> districtHashMap, Map<String, Taluka> talukaHashMap);
}
