package org.motechproject.nms.rejectionhandler.service.impl;

import org.motechproject.nms.rejectionhandler.domain.VillageImportRejection;
import org.motechproject.nms.rejectionhandler.repository.VillageRejectionDataService;
import org.motechproject.nms.rejectionhandler.service.VillageRejectionService;
import org.springframework.beans.factory.annotation.Autowired;

public class VillageRejectionServiceImpl implements VillageRejectionService {
    @Autowired
    private VillageRejectionDataService villageRejectionDataService;

    @Override
    public void createUpdateRejectedVillage(VillageImportRejection villageImportRejection) {

    }
}
