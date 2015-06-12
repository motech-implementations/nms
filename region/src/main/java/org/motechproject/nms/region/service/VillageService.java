package org.motechproject.nms.region.service;

import org.motechproject.nms.region.domain.Taluka;
import org.motechproject.nms.region.domain.Village;

public interface VillageService {
    Village findByTalukaAndVcodeAndSvid(Taluka taluka, Long vcode, Long svid);
    Village findByTalukaAndSvid(Taluka taluka, Long svid);
}
