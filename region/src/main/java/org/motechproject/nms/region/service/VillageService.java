package org.motechproject.nms.region.service;

import org.motechproject.nms.region.domain.Taluka;
import org.motechproject.nms.region.domain.Village;

import java.util.List;
import java.util.Map;

public interface VillageService {
    Village findByTalukaAndVcodeAndSvid(Taluka taluka, long vcode, long svid);
    Village create(Village village);
    Village update(Village village);

    Long createUpdateVillages(List<Map<String, Object>> recordList, Map<String, Taluka> talukaHashMap);
}
