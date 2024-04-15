package org.motechproject.nms.rejectionhandler.service.impl;

import org.motechproject.mds.query.SqlQueryExecution;
import org.motechproject.nms.rejectionhandler.domain.VillageImportRejection;
import org.motechproject.nms.rejectionhandler.repository.VillageRejectionDataService;
import org.motechproject.nms.rejectionhandler.service.VillageRejectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.jdo.Query;
import javax.jdo.annotations.Transactional;

@Service("villageRejectionService")
public class VillageRejectionServiceImpl implements VillageRejectionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(VillageRejectionServiceImpl.class);

    @Autowired
    private VillageRejectionDataService villageRejectionDataService;

    @Override
    @Transactional
    public Long saveRejectedVillageInBulk(String rejectedVillageValues) {
        SqlQueryExecution<Long> queryExecution = new SqlQueryExecution<Long>() {
            @Override
            public String getSqlQuery() {
                String query = "INSERT into nms_village_rejects (`stateId`, `districtCode`, `talukaCode`, `villageCode`, `villageName`," +
                        " `accepted`, `rejectionReason`, `creator`, `modifiedBy`, `owner`, `creationDate`, `modificationDate`, `svid`, `mddsCode`, `state_code`) VALUES " +
                        rejectedVillageValues + " ON DUPLICATE KEY UPDATE " +
                        "districtCode = VALUES(districtCode), talukaCode = VALUES(talukaCode), villageName = VALUES(villageName), accepted = VALUES(accepted), rejectionReason = VALUES(rejectionReason),modificationDate = VALUES(modificationDate), modifiedBy = VALUES(modifiedBy), mddsCode=VALUES(mddsCode), state_code=VALUES(state_code) ";
                LOGGER.info("Printing Query for rejected Village: "+ query);
                return query;
            }
            @Override
            public Long execute(Query query) {
                query.setClass(VillageImportRejection.class);
                return (Long) query.execute();
            }

        };
        Long rejectedVillage = 0L;

        if(!rejectedVillageValues.isEmpty()){
            rejectedVillage = villageRejectionDataService.executeSQLQuery(queryExecution);
        }

        return rejectedVillage;
    }

}
