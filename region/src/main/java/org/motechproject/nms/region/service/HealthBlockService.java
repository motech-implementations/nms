package org.motechproject.nms.region.service;

import org.motechproject.nms.region.domain.HealthBlock;
import org.motechproject.nms.region.domain.Taluka;

public interface HealthBlockService {
    HealthBlock findByTalukaAndCode(Taluka taluka, Long code);
    void create(HealthBlock healthBlock);
    void update(HealthBlock healthBlock);
}
