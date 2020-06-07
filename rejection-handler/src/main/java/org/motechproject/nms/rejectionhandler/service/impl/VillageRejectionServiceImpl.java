package org.motechproject.nms.rejectionhandler.service.impl;

import org.motechproject.nms.rejectionhandler.domain.VillageImportRejection;
import org.motechproject.nms.rejectionhandler.repository.VillageRejectionDataService;
import org.motechproject.nms.rejectionhandler.service.VillageRejectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("villageRejectionService")
public class VillageRejectionServiceImpl implements VillageRejectionService {
    @Autowired
    private VillageRejectionDataService villageRejectionDataService;

    @Override
    public void saveRejectedVillage(VillageImportRejection villageImportRejection) {
        villageRejectionDataService.create(villageImportRejection);

    }
}
