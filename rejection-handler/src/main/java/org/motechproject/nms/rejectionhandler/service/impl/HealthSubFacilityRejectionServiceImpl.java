package org.motechproject.nms.rejectionhandler.service.impl;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.motechproject.mds.query.SqlQueryExecution;
import org.motechproject.nms.rejectionhandler.domain.HealthFacilityImportRejection;
import org.motechproject.nms.rejectionhandler.domain.HealthSubFacilityImportRejection;
import org.motechproject.nms.rejectionhandler.repository.HealthSubFacilityRejectionDataService;
import org.motechproject.nms.rejectionhandler.service.HealthSubFacilityRejectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.jdo.Query;
import javax.jdo.annotations.Transactional;

@Service("healthSubFacilityRejectionService")
public class HealthSubFacilityRejectionServiceImpl implements HealthSubFacilityRejectionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(HealthSubFacilityRejectionServiceImpl.class);
    private static final String MOTECH_STRING = "'motech', ";
    private static final String DATE_FORMAT_STRING = "yyyy-MM-dd HH:mm:ss";

    @Autowired
    private HealthSubFacilityRejectionDataService healthSubFacilityRejectionDataService;

    @Override
    @Transactional
    public Long saveRejectedHealthSubFacility(HealthSubFacilityImportRejection healthSubFacilityImportRejection) {
        SqlQueryExecution<Long> queryExecution = new SqlQueryExecution<Long>() {
            @Override
            public String getSqlQuery() {
                DateTime dateTimeNow = new DateTime();
                DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(DATE_FORMAT_STRING);
                LOGGER.info("In getSqlQuery");
                String healthSubFacilityValues = "(" + healthSubFacilityImportRejection.getStateId() + ", " + healthSubFacilityImportRejection.getDistrictCode() + ", '" + healthSubFacilityImportRejection.getTalukaCode() + "', " +
                        healthSubFacilityImportRejection.getHealthFacilityCode()+", "+healthSubFacilityImportRejection.getHealthSubFacilityCode()+", '" + healthSubFacilityImportRejection.getHealthSubFacilityName() + "', " + healthSubFacilityImportRejection.getAccepted() + ", '" + healthSubFacilityImportRejection.getRejectionReason() +"', "+MOTECH_STRING+MOTECH_STRING+MOTECH_STRING+"'"+ dateTimeFormatter.print(dateTimeNow)+"', '"+dateTimeFormatter.print(dateTimeNow)+"')";
                LOGGER.info(healthSubFacilityValues);
                String query = "INSERT into nms_health_sub_facility_rejects (`stateId`, `districtCode`, `talukaCode`, `healthFacilityCode`, `healthSubFacilityCode`, `healthSubFacilityName`," +
                        " `accepted`, `rejectionReason`, `creator`, `modifiedBy`, `owner`, `creationDate`, `modificationDate`) VALUES " +
                        healthSubFacilityValues + " ON DUPLICATE KEY UPDATE " +
                        "districtCode = VALUES(districtCode), talukaCode = VALUES(talukaCode),healthFacilityCode = VALUES(healthFacilityCode), healthSubFacilityName = VALUES(healthSubFacilityName), accepted = VALUES(accepted), rejectionReason = VALUES(rejectionReason),modificationDate = VALUES(modificationDate), modifiedBy = VALUES(modifiedBy) ";
                LOGGER.info("Printing Query for rejected HSF: "+ query);
                return query;


            }
            @Override
            public Long execute(Query query) {
                query.setClass(HealthSubFacilityImportRejection.class);
                LOGGER.info("HSF class reject query: " + query.toString());
                return (Long) query.execute();
            }

        };
        Long rejectedHealthSubFacility = 0L;
        LOGGER.info("Printing HSF rejection query execution: " + queryExecution.toString());

        rejectedHealthSubFacility = healthSubFacilityRejectionDataService.executeSQLQuery(queryExecution);

        return rejectedHealthSubFacility;
    }

    @Override
    public void createRejectedHealthSubFacility(HealthSubFacilityImportRejection healthSubFacilityImportRejection) {
        healthSubFacilityRejectionDataService.create(healthSubFacilityImportRejection);

    }
    @Override
    @Transactional
    public Long saveRejectedHealthSubFacilityInBulk(String rejectedHealthSubFacilityValues) {
        SqlQueryExecution<Long> queryExecution = new SqlQueryExecution<Long>() {
            @Override
            public String getSqlQuery() {
                String query = "INSERT into nms_health_sub_facility_rejects (`stateId`, `districtCode`, `talukaCode`, `healthFacilityCode`, `healthSubFacilityCode`, `healthSubFacilityName`," +
                        " `accepted`, `rejectionReason`, `creator`, `modifiedBy`, `owner`, `creationDate`, `modificationDate`) VALUES " +
                        rejectedHealthSubFacilityValues + " ON DUPLICATE KEY UPDATE " +
                        "districtCode = VALUES(districtCode), talukaCode = VALUES(talukaCode),healthFacilityCode = VALUES(healthFacilityCode), healthSubFacilityName = VALUES(healthSubFacilityName), accepted = VALUES(accepted), rejectionReason = VALUES(rejectionReason),modificationDate = VALUES(modificationDate), modifiedBy = VALUES(modifiedBy) ";
                LOGGER.info("Printing Query for rejected HSF: "+ query);
                return query;


            }
            @Override
            public Long execute(Query query) {
                query.setClass(HealthSubFacilityImportRejection.class);
                return (Long) query.execute();
            }

        };
        Long rejectedHealthSubFacility = 0L;

        if(!rejectedHealthSubFacilityValues.isEmpty()){
            rejectedHealthSubFacility = healthSubFacilityRejectionDataService.executeSQLQuery(queryExecution);

        }
        return rejectedHealthSubFacility;
    }

}
