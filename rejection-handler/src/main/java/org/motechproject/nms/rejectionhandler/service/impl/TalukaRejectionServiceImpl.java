package org.motechproject.nms.rejectionhandler.service.impl;

import org.motechproject.nms.rejectionhandler.domain.TalukaImportRejection;
import org.motechproject.nms.rejectionhandler.repository.TalukaRejectionDataService;
import org.motechproject.nms.rejectionhandler.service.TalukaRejectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("talukaRejectionService")
public class TalukaRejectionServiceImpl implements TalukaRejectionService {

    @Autowired
    TalukaRejectionDataService talukaRejectionDataService;

    @Override
    public void saveRejectedTaluka(TalukaImportRejection talukaImportRejection){
        if(   talukaRejectionDataService.findByUniqueCode(talukaImportRejection.getStateId(),talukaImportRejection.getTalukaCode()) == null){
            talukaRejectionDataService.create(talukaImportRejection);
        }
        else {
            talukaRejectionDataService.update(talukaImportRejection);
        }
    }
    @Override
    public void createRejectedTaluka(TalukaImportRejection talukaImportRejection){
        talukaRejectionDataService.create(talukaImportRejection);

    }
}
