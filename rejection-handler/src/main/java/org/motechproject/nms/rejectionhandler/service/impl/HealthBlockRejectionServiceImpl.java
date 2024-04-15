package org.motechproject.nms.rejectionhandler.service.impl;

import org.motechproject.mds.query.SqlQueryExecution;
import org.motechproject.nms.rejectionhandler.domain.HealthBlockImportRejection;
import org.motechproject.nms.rejectionhandler.repository.HealthBlockRejectionDataService;
import org.motechproject.nms.rejectionhandler.service.HealthBlockRejectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.jdo.Query;
import javax.jdo.annotations.Transactional;

@Service("healthBlockRejectionService")
public class HealthBlockRejectionServiceImpl implements HealthBlockRejectionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(HealthBlockRejectionServiceImpl.class);

    @Autowired
    private HealthBlockRejectionDataService healthBlockRejectionDataService;

    @Override
    @Transactional
    public Long saveRejectedHealthBlockInBulk(String rejectedHealthBlockValues) {
        SqlQueryExecution<Long> queryExecution = new SqlQueryExecution<Long>() {
            @Override
            public String getSqlQuery() {
                String query = "INSERT into nms_health_block_rejects (`stateId`, `districtCode`, `talukaCode`, `healthBlockCode`, `healthBlockName`, " +
                        " `accepted`, `rejectionReason`, `creator`, `modifiedBy`, `owner`, `creationDate`, `modificationDate`, `mddsCode`) VALUES " +
                        rejectedHealthBlockValues + " ON DUPLICATE KEY UPDATE " +
                        "districtCode = VALUES(districtCode), talukaCode = VALUES(talukaCode), healthBlockName = VALUES(healthBlockName), accepted = VALUES(accepted), rejectionReason = VALUES(rejectionReason),modificationDate = VALUES(modificationDate), modifiedBy = VALUES(modifiedBy), mddsCode=VALUES(mddsCode) ";
                LOGGER.info("Printing Query for rejected HB: "+ query);
                return query;

            }
            @Override
            public Long execute(Query query) {
                query.setClass(HealthBlockImportRejection.class);
                return (Long) query.execute();
            }

        };
        Long rejectedHealthBlock = 0L;

        if(!rejectedHealthBlockValues.isEmpty()){
            rejectedHealthBlock = healthBlockRejectionDataService.executeSQLQuery(queryExecution);
        }
        return rejectedHealthBlock;
    }

}
