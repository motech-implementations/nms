package org.motechproject.nms.rejectionhandler.service.impl;

import org.motechproject.nms.rejectionhandler.domain.DistrictImportRejection;
import org.motechproject.nms.rejectionhandler.repository.DistrictRejectionDataService;
import org.motechproject.nms.rejectionhandler.service.DistrictRejectionService;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("districtRejectionService")
public class DistrictRejectionServiceImpl implements DistrictRejectionService {

    @Autowired
    DistrictRejectionDataService districtRejectionDataService;

    @Override
    public void createRejectedDistrict(DistrictImportRejection districtImportRejection) {
        districtRejectionDataService.create(districtImportRejection);

    }

    @Override
    public void saveRejectedDistrict(DistrictImportRejection districtImportRejection) {
        if (districtRejectionDataService.findByUniqueCode(districtImportRejection.getStateId(), districtImportRejection.getDistrictCode()) == null) {
            districtRejectionDataService.create(districtImportRejection);
        } else {
            districtRejectionDataService.update(districtImportRejection);
        }
    }


}
