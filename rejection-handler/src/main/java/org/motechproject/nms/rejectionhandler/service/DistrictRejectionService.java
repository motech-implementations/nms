package org.motechproject.nms.rejectionhandler.service;

import org.motechproject.nms.rejectionhandler.domain.DistrictImportRejection;
import org.springframework.stereotype.Service;


public interface DistrictRejectionService {

    void createRejectedDistrict(DistrictImportRejection districtImportRejection);
    void saveRejectedDistrict(DistrictImportRejection districtImportRejection);
}
