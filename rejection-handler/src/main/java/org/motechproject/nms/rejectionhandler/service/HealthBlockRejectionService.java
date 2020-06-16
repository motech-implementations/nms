package org.motechproject.nms.rejectionhandler.service;

import org.motechproject.nms.rejectionhandler.domain.HealthBlockImportRejection;
import org.springframework.stereotype.Service;

public interface HealthBlockRejectionService {
    void createRejectedHealthBlock(HealthBlockImportRejection healthBlockImportRejection);
    Long saveRejectedHealthBlock(HealthBlockImportRejection healthBlockImportRejection);
}
