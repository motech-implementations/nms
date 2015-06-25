package org.motechproject.nms.region.service;

import org.motechproject.nms.region.domain.District;
import org.motechproject.nms.region.domain.Taluka;

public interface TalukaService {
    Taluka findByDistrictAndCode(District district, String code);
}
