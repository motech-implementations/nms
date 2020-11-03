package org.motechproject.nms.rejectionhandler.service.impl;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
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
    private static final String MOTECH_STRING = "'motech', ";
    private static final String DATE_FORMAT_STRING = "yyyy-MM-dd HH:mm:ss";

    @Autowired
    private HealthBlockRejectionDataService healthBlockRejectionDataService;


    @Override
    @Transactional
    public Long saveRejectedHealthBlock(HealthBlockImportRejection healthBlockImportRejection) {
        LOGGER.info("In saveRejectedHealthBlock");
        SqlQueryExecution<Long> queryExecution = new SqlQueryExecution<Long>() {
            @Override
            public String getSqlQuery() {
                DateTime dateTimeNow = new DateTime();
                DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(DATE_FORMAT_STRING);
                LOGGER.info("In getSqlQuery");
                String healthBlockValues = "(" + healthBlockImportRejection.getStateId() + ", " + healthBlockImportRejection.getDistrictCode() + ", '" + healthBlockImportRejection.getTalukaCode() + "', " +
                        healthBlockImportRejection.getHealthBlockCode() + ", '" + healthBlockImportRejection.getHealthBlockName() + "', " + healthBlockImportRejection.getAccepted() + ", '" + healthBlockImportRejection.getRejectionReason() +"', "+MOTECH_STRING+MOTECH_STRING+MOTECH_STRING+"'"+ dateTimeFormatter.print(dateTimeNow)+"', '"+dateTimeFormatter.print(dateTimeNow)+"')";
                LOGGER.info(healthBlockValues);
                String query = "INSERT into nms_health_block_rejects (`stateId`, `districtCode`, `talukaCode`, `healthBlockCode`, `healthBlockName`, " +
                        " `accepted`, `rejectionReason`, `creator`, `modifiedBy`, `owner`, `creationDate`, `modificationDate`) VALUES " +
                        healthBlockValues + " ON DUPLICATE KEY UPDATE " +
                        "districtCode = VALUES(districtCode), talukaCode = VALUES(talukaCode), healthBlockName = VALUES(healthBlockName), accepted = VALUES(accepted), rejectionReason = VALUES(rejectionReason),modificationDate = VALUES(modificationDate), modifiedBy = VALUES(modifiedBy) ";
                LOGGER.info("Printing Query for rejected HB: "+ query);
                return query;

            }
            @Override
            public Long execute(Query query) {
                query.setClass(HealthBlockImportRejection.class);
                LOGGER.info("HB class reject query: " + query.toString());
                return (Long) query.execute();
            }

        };
        Long rejectedHealthBlock = 0L;
        LOGGER.info("Printing HB rejection query execution: " + queryExecution.toString());

        rejectedHealthBlock = healthBlockRejectionDataService.executeSQLQuery(queryExecution);

        return rejectedHealthBlock;
    }

    @Override
    public void createRejectedHealthBlock(HealthBlockImportRejection healthBlockImportRejection) {
        healthBlockRejectionDataService.create(healthBlockImportRejection);
    }

    @Override
    @Transactional
    public Long saveRejectedHealthBlockInBulk(String rejectedHealthBlockValues) {
        SqlQueryExecution<Long> queryExecution = new SqlQueryExecution<Long>() {
            @Override
            public String getSqlQuery() {
                String query = "INSERT into nms_health_block_rejects (`stateId`, `districtCode`, `talukaCode`, `healthBlockCode`, `healthBlockName`, " +
                        " `accepted`, `rejectionReason`, `creator`, `modifiedBy`, `owner`, `creationDate`, `modificationDate`) VALUES " +
                        rejectedHealthBlockValues + " ON DUPLICATE KEY UPDATE " +
                        "districtCode = VALUES(districtCode), talukaCode = VALUES(talukaCode), healthBlockName = VALUES(healthBlockName), accepted = VALUES(accepted), rejectionReason = VALUES(rejectionReason),modificationDate = VALUES(modificationDate), modifiedBy = VALUES(modifiedBy) ";
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
