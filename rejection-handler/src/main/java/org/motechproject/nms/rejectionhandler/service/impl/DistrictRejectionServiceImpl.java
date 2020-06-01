package org.motechproject.nms.rejectionhandler.service.impl;

import org.motechproject.nms.rejectionhandler.domain.DistrictImportRejection;
import org.motechproject.nms.rejectionhandler.repository.DistrictRejectionDataService;
import org.motechproject.nms.rejectionhandler.service.DistrictRejectionService;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class DistrictRejectionServiceImpl implements DistrictRejectionService {

    @Autowired
    DistrictRejectionDataService districtRejectionDataService;

    @Override
    public void createUpdateRejectedDistrict(DistrictImportRejection districtImportRejection) {

    }
}
