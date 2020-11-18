package org.motechproject.nms.rejectionhandler.service.impl;

import org.motechproject.mds.query.SqlQueryExecution;
import org.motechproject.nms.rejectionhandler.domain.TalukaImportRejection;
import org.motechproject.nms.rejectionhandler.repository.TalukaRejectionDataService;
import org.motechproject.nms.rejectionhandler.service.TalukaRejectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.jdo.Query;
import javax.jdo.annotations.Transactional;

@Service("talukaRejectionService")
public class TalukaRejectionServiceImpl implements TalukaRejectionService {


    private static final Logger LOGGER = LoggerFactory.getLogger(TalukaRejectionServiceImpl.class);

    @Autowired
    TalukaRejectionDataService talukaRejectionDataService;

    @Override
    @Transactional
    public Long saveRejectedTalukaInBulk(String talukarejectedValues){
        SqlQueryExecution<Long> queryExecution = new SqlQueryExecution<Long>() {
            @Override
            public String getSqlQuery() {
                String query = "INSERT into nms_taluka_rejects (`stateId`, `districtCode`, `talukaCode`, `talukaName`," +
                        " `accepted`, `rejectionReason`, `creator`, `modifiedBy`, `owner`, `creationDate`, `modificationDate`) VALUES " +
                        talukarejectedValues + " ON DUPLICATE KEY UPDATE " +
                        "districtCode = VALUES(districtCode), talukaName = VALUES(talukaName), accepted = VALUES(accepted), rejectionReason = VALUES(rejectionReason),modificationDate = VALUES(modificationDate), modifiedBy = VALUES(modifiedBy) ";
                LOGGER.info("Printing Query for rejected Taluka: "+ query);
                return query;

            }
            @Override
            public Long execute(Query query) {
                query.setClass(TalukaImportRejection.class);
                return (Long) query.execute();
            }

        };
        Long rejectedTaluka = 0L;
        if(!talukarejectedValues.isEmpty()){
            rejectedTaluka = talukaRejectionDataService.executeSQLQuery(queryExecution);

        }
        return rejectedTaluka;
    }
}
