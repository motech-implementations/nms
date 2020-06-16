package org.motechproject.nms.rejectionhandler.service;

import org.motechproject.nms.rejectionhandler.domain.DistrictImportRejection;


public interface DistrictRejectionService {

    void createRejectedDistrict(DistrictImportRejection districtImportRejection);
    Long saveRejectedDistrict(DistrictImportRejection districtImportRejection);
}
