package org.motechproject.nms.rejectionhandler.service;

import org.motechproject.nms.rejectionhandler.domain.VillageImportRejection;
import org.springframework.stereotype.Service;

public interface VillageRejectionService {

    void saveRejectedVillage(VillageImportRejection villageImportRejection);
    void createRejectedVillage(VillageImportRejection villageImportRejection);

}
