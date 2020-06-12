package org.motechproject.nms.rejectionhandler.service.impl;

import org.motechproject.nms.rejectionhandler.domain.VillageHealthSubFacilityImportRejection;
import org.motechproject.nms.rejectionhandler.repository.VillageHealthSubFacilityRejectionDataService;
import org.motechproject.nms.rejectionhandler.service.VillageHealthSubFacilityRejectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("villageHealthSubFacilityRejectionService")
public class VillageHealthSubFacilityRejectionServiceImpl implements VillageHealthSubFacilityRejectionService {

    @Autowired
    private VillageHealthSubFacilityRejectionDataService villageHealthSubFacilityRejectionDataService;

    @Override
    public void createRejectedVillageHealthSubFacility(VillageHealthSubFacilityImportRejection villageHealthSubFacilityImportRejection) {
        villageHealthSubFacilityRejectionDataService.create(villageHealthSubFacilityImportRejection);
    }

    @Override
    public void saveRejectedVillageHealthSubFacility(VillageHealthSubFacilityImportRejection villageHealthSubFacilityImportRejection) {
        if(   villageHealthSubFacilityRejectionDataService.findByUniqueCode(villageHealthSubFacilityImportRejection.getVillageCode(),villageHealthSubFacilityImportRejection.getHealthSubFacilityCode()) == null){
            villageHealthSubFacilityRejectionDataService.create(villageHealthSubFacilityImportRejection);
        }
        else {
            villageHealthSubFacilityRejectionDataService.update(villageHealthSubFacilityImportRejection);
        }
    }

}
