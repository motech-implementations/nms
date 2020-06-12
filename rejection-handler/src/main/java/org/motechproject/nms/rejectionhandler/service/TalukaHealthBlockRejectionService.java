package org.motechproject.nms.rejectionhandler.service;

import org.motechproject.nms.rejectionhandler.domain.TalukaHealthBlockImportRejection;

public interface TalukaHealthBlockRejectionService {

    void createRejectedTalukaHealthBlock(TalukaHealthBlockImportRejection talukaHealthBlockImportRejection);
    void saveRejectedTalukaHealthBlock(TalukaHealthBlockImportRejection talukaHealthBlockImportRejection);
}
