package org.motechproject.nms.rejectionhandler.service;

import org.motechproject.nms.rejectionhandler.domain.DistrictImportRejection;

import java.util.Map;

public interface DistrictRejectionService {

    void createUpdateRejectedDistrict(DistrictImportRejection districtImportRejection);
}
