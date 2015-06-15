package org.motechproject.nms.region.service;

import org.motechproject.nms.region.domain.Taluka;
import org.motechproject.nms.region.domain.Village;

public interface VillageService {
    Village findByTalukaAndVcodeAndSvid(Taluka taluka, long vcode, long svid);
    Village findByTalukaAndSvid(Taluka taluka, long svid);
}
