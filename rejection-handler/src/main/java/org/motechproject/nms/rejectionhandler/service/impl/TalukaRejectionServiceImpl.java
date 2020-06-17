package org.motechproject.nms.rejectionhandler.service.impl;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.motechproject.mds.query.SqlQueryExecution;
import org.motechproject.nms.rejectionhandler.domain.HealthFacilityImportRejection;
import org.motechproject.nms.rejectionhandler.domain.TalukaImportRejection;
import org.motechproject.nms.rejectionhandler.repository.TalukaRejectionDataService;
import org.motechproject.nms.rejectionhandler.service.TalukaRejectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.jdo.Query;

@Service("talukaRejectionService")
public class TalukaRejectionServiceImpl implements TalukaRejectionService {


    private static final Logger LOGGER = LoggerFactory.getLogger(TalukaRejectionServiceImpl.class);
    private static final String MOTECH_STRING = "'motech', ";
    private static final String DATE_FORMAT_STRING = "yyyy-MM-dd HH:mm:ss";

    @Autowired
    TalukaRejectionDataService talukaRejectionDataService;

    @Override
    public Long saveRejectedTaluka(TalukaImportRejection talukaImportRejection){
        SqlQueryExecution<Long> queryExecution = new SqlQueryExecution<Long>() {
            @Override
            public String getSqlQuery() {
                DateTime dateTimeNow = new DateTime();
                DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(DATE_FORMAT_STRING);
                LOGGER.info("In getSqlQuery");
                String healthBlockValues = "(" + talukaImportRejection.getStateId() + ", " + talukaImportRejection.getDistrictCode() + ", '" + talukaImportRejection.getTalukaCode() + "', '" +
                        talukaImportRejection.getTalukaName() + "', " + talukaImportRejection.getAccepted() + ", '" + talukaImportRejection.getRejectionReason() +"', "+MOTECH_STRING+MOTECH_STRING+MOTECH_STRING+"'"+ dateTimeFormatter.print(dateTimeNow)+"', '"+dateTimeFormatter.print(dateTimeNow)+"')";
                LOGGER.info(healthBlockValues);
                String query = "INSERT into nms_taluka_rejects (`stateId`, `districtCode`, `talukaCode`, `talukaName`," +
                        " `accepted`, `rejectionReason`, `creator`, `modifiedBy`, `owner`, `creationDate`, `modificationDate`) VALUES " +
                        healthBlockValues + " ON DUPLICATE KEY UPDATE " +
                        "districtCode = VALUES(districtCode), talukaName = VALUES(talukaName), accepted = VALUES(accepted), rejectionReason = VALUES(rejectionReason),modificationDate = VALUES(modificationDate), modifiedBy = VALUES(modifiedBy) ";
                LOGGER.info("Printing Query for rejected Taluka: "+ query);
                return query;


            }
            @Override
            public Long execute(Query query) {
                query.setClass(TalukaImportRejection.class);
                LOGGER.info("Taluka class reject query: " + query.toString());
                return (Long) query.execute();
            }

        };
        Long rejectedTaluka = 0L;
        LOGGER.info("Printing Taluka rejection query execution: " + queryExecution.toString());

        rejectedTaluka = talukaRejectionDataService.executeSQLQuery(queryExecution);

        return rejectedTaluka;
    }
    @Override
    public void createRejectedTaluka(TalukaImportRejection talukaImportRejection){
        talukaRejectionDataService.create(talukaImportRejection);

    }
}
