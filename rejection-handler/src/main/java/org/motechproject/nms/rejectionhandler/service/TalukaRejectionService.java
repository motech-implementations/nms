package org.motechproject.nms.rejectionhandler.service;

import org.motechproject.nms.rejectionhandler.domain.TalukaImportRejection;
import org.springframework.stereotype.Service;

public interface TalukaRejectionService {
    Long saveRejectedTaluka(TalukaImportRejection talukaImportRejection);
    void createRejectedTaluka(TalukaImportRejection talukaImportRejection);

}
