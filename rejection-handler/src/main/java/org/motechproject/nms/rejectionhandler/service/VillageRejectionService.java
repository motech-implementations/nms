package org.motechproject.nms.rejectionhandler.service;

import org.motechproject.nms.rejectionhandler.domain.VillageImportRejection;

public interface VillageRejectionService {
    void createUpdateRejectedVillage(VillageImportRejection villageImportRejection);
}
