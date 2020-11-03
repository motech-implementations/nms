package org.motechproject.nms.rejectionhandler.service;

import org.motechproject.nms.rejectionhandler.domain.VillageImportRejection;
import org.springframework.stereotype.Service;

public interface VillageRejectionService {

    Long saveRejectedVillage(VillageImportRejection villageImportRejection);
    void createRejectedVillage(VillageImportRejection villageImportRejection);
    Long saveRejectedVillageInBulk(String rejectedVillageValues);
}
