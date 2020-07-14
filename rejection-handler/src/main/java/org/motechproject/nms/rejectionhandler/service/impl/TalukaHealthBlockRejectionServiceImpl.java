package org.motechproject.nms.rejectionhandler.service.impl;

import org.motechproject.nms.rejectionhandler.domain.TalukaHealthBlockImportRejection;
import org.motechproject.nms.rejectionhandler.repository.TalukaHealthBlockRejectionDataService;
import org.motechproject.nms.rejectionhandler.service.TalukaHealthBlockRejectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("talukaHealthBlockRejectionService")
public class TalukaHealthBlockRejectionServiceImpl implements TalukaHealthBlockRejectionService {
    @Autowired
    private TalukaHealthBlockRejectionDataService talukaHealthBlockRejectionDataService;

    @Override
    public void createRejectedTalukaHealthBlock(TalukaHealthBlockImportRejection talukaHealthBlockImportRejection) {
       talukaHealthBlockRejectionDataService.create(talukaHealthBlockImportRejection);
    }

    @Override
    public void saveRejectedTalukaHealthBlock(TalukaHealthBlockImportRejection talukaHealthBlockImportRejection) {
        TalukaHealthBlockImportRejection talukaHealthBlockImportRejection1= talukaHealthBlockRejectionDataService.findByUniqueCode(talukaHealthBlockImportRejection.getHealthBlockCode(),talukaHealthBlockImportRejection.getTalukaCode());
        if( talukaHealthBlockImportRejection1  == null){
            talukaHealthBlockRejectionDataService.create(talukaHealthBlockImportRejection);
        }
        else {
            /** this update does not work, TODO: when implementing for mapping locations this needs to be changed */
            talukaHealthBlockRejectionDataService.update(talukaHealthBlockImportRejection);
        }
    }

}
