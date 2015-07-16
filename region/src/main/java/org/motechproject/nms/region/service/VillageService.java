package org.motechproject.nms.region.service;

import org.motechproject.nms.region.domain.Taluka;
import org.motechproject.nms.region.domain.Village;

public interface VillageService {
    Village findByTalukaAndVcodeAndSvid(Taluka taluka, long vcode, long svid);
    void create(Village village);
    void update(Village village);
}
