package org.motechproject.nms.rejectionhandler.service.impl;

import org.motechproject.nms.rejectionhandler.domain.TalukaImportRejection;
import org.motechproject.nms.rejectionhandler.repository.TalukaRejectionDataService;
import org.motechproject.nms.rejectionhandler.service.TalukaRejectionService;
import org.springframework.beans.factory.annotation.Autowired;

public class TalukaRejectionServiceImpl implements TalukaRejectionService {

    @Autowired
    TalukaRejectionDataService talukaRejectionDataService;

    @Override
    public void createUpdateRejectedTaluka(TalukaImportRejection talukaImportRejection){        talukaRejectionDataService.createOrUpdate(talukaImportRejection);

    }
}
