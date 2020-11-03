package org.motechproject.nms.rejectionhandler.service.impl;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.motechproject.mds.query.SqlQueryExecution;
import org.motechproject.nms.rejectionhandler.domain.HealthFacilityImportRejection;
import org.motechproject.nms.rejectionhandler.repository.HealthFacilityRejectionDataService;
import org.motechproject.nms.rejectionhandler.service.HealthFacilityRejectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.jdo.Query;
import javax.jdo.annotations.Transactional;

@Service("healthFacilityRejectionService")
public class HealthFacilityRejectionServiceImpl implements HealthFacilityRejectionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(HealthFacilityRejectionServiceImpl.class);
    private static final String MOTECH_STRING = "'motech', ";
    private static final String DATE_FORMAT_STRING = "yyyy-MM-dd HH:mm:ss";

    @Autowired
    HealthFacilityRejectionDataService healthFacilityRejectionDataService;


    @Override
    @Transactional
    public Long saveRejectedHealthFacility(HealthFacilityImportRejection healthFacilityImportRejection) {
        SqlQueryExecution<Long> queryExecution = new SqlQueryExecution<Long>() {
            @Override
            public String getSqlQuery() {
                DateTime dateTimeNow = new DateTime();
                DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(DATE_FORMAT_STRING);
                LOGGER.info("In getSqlQuery");
                String healthFacilityValues = "(" + healthFacilityImportRejection.getStateId() + ", " + healthFacilityImportRejection.getDistrictCode() + ", '" + healthFacilityImportRejection.getTalukaCode() + "', " +
                        healthFacilityImportRejection.getHealthBlockCode() +", "+ healthFacilityImportRejection.getHealthFacilityCode()+", '" + healthFacilityImportRejection.getHealthFacilityName() + "', " + healthFacilityImportRejection.getAccepted() + ", '" + healthFacilityImportRejection.getRejectionReason() +"', "+MOTECH_STRING+MOTECH_STRING+MOTECH_STRING+"'"+ dateTimeFormatter.print(dateTimeNow)+"', '"+dateTimeFormatter.print(dateTimeNow)+"')";
                LOGGER.info(healthFacilityValues);
                String query = "INSERT into nms_health_facility_rejects (`stateId`, `districtCode`, `talukaCode`, `healthBlockCode`, `healthFacilityCode`, `healthFacilityName`," +
                        " `accepted`, `rejectionReason`, `creator`, `modifiedBy`, `owner`, `creationDate`, `modificationDate`) VALUES " +
                        healthFacilityValues + " ON DUPLICATE KEY UPDATE " +
                        "districtCode = VALUES(districtCode), talukaCode = VALUES(talukaCode),healthBlockCode = VALUES(healthBlockCode), healthFacilityName = VALUES(healthFacilityName), accepted = VALUES(accepted), rejectionReason = VALUES(rejectionReason),modificationDate = VALUES(modificationDate), modifiedBy = VALUES(modifiedBy) ";
                LOGGER.info("Printing Query for rejected HF: "+ query);
                return query;

            }
            @Override
            public Long execute(Query query) {
                query.setClass(HealthFacilityImportRejection.class);
                LOGGER.info("HF class reject query: " + query.toString());
                return (Long) query.execute();
            }

        };
        Long rejectedHealthFacility = 0L;
        LOGGER.info("Printing HF rejection query execution: " + queryExecution.toString());

        rejectedHealthFacility = healthFacilityRejectionDataService.executeSQLQuery(queryExecution);

        return rejectedHealthFacility;
    }
    @Override
    public void createRejectedHealthFacility(HealthFacilityImportRejection healthFacilityImportRejection) {
        healthFacilityRejectionDataService.create(healthFacilityImportRejection);
    }

    @Override
    @Transactional
    public Long saveRejectedHealthFacilityInBulk(String rejectedHealthFacilityValues) {
        SqlQueryExecution<Long> queryExecution = new SqlQueryExecution<Long>() {
            @Override
            public String getSqlQuery() {
                String query = "INSERT into nms_health_facility_rejects (`stateId`, `districtCode`, `talukaCode`, `healthBlockCode`, `healthFacilityCode`, `healthFacilityName`," +
                        " `accepted`, `rejectionReason`, `creator`, `modifiedBy`, `owner`, `creationDate`, `modificationDate`) VALUES " +
                        rejectedHealthFacilityValues + " ON DUPLICATE KEY UPDATE " +
                        "districtCode = VALUES(districtCode), talukaCode = VALUES(talukaCode),healthBlockCode = VALUES(healthBlockCode), healthFacilityName = VALUES(healthFacilityName), accepted = VALUES(accepted), rejectionReason = VALUES(rejectionReason),modificationDate = VALUES(modificationDate), modifiedBy = VALUES(modifiedBy) ";
                LOGGER.info("Printing Query for rejected HF: "+ query);
                return query;

            }
            @Override
            public Long execute(Query query) {
                query.setClass(HealthFacilityImportRejection.class);
                return (Long) query.execute();
            }

        };
        Long rejectedHealthFacility = 0L;

        if(!rejectedHealthFacilityValues.isEmpty()){
            rejectedHealthFacility = healthFacilityRejectionDataService.executeSQLQuery(queryExecution);
        }
        return rejectedHealthFacility;
    }
}
