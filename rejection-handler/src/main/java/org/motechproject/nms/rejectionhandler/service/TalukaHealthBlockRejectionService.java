package org.motechproject.nms.rejectionhandler.service;

import org.motechproject.nms.rejectionhandler.domain.TalukaHealthBlockImportRejection;

public interface TalukaHealthBlockRejectionService {
    void saveRejectedTalukaHealthBlock(TalukaHealthBlockImportRejection talukaHealthBlockImportRejection);
}
