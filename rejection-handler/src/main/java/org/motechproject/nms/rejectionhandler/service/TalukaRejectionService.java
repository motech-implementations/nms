package org.motechproject.nms.rejectionhandler.service;

import org.motechproject.nms.rejectionhandler.domain.TalukaImportRejection;

public interface TalukaRejectionService {
    void createUpdateRejectedTaluka(TalukaImportRejection talukaImportRejection);
}
