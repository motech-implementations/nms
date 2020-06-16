package org.motechproject.nms.rejectionhandler.service.impl;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.motechproject.mds.query.SqlQueryExecution;
import org.motechproject.nms.rejectionhandler.domain.DistrictImportRejection;
import org.motechproject.nms.rejectionhandler.repository.DistrictRejectionDataService;
import org.motechproject.nms.rejectionhandler.service.DistrictRejectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.jdo.Query;
import javax.jdo.annotations.Transactional;

@Service("districtRejectionService")
public class DistrictRejectionServiceImpl implements DistrictRejectionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DistrictRejectionServiceImpl.class);
    private static final String MOTECH_STRING = "'motech', ";
    private static final String DATE_FORMAT_STRING = "yyyy-MM-dd HH:mm:ss";

    @Autowired
    DistrictRejectionDataService districtRejectionDataService;

    @Override
    public void createRejectedDistrict(DistrictImportRejection districtImportRejection) {
        districtRejectionDataService.create(districtImportRejection);

    }

    @Override
    @Transactional
    public Long saveRejectedDistrict(DistrictImportRejection districtImportRejection) {
        SqlQueryExecution<Long> queryExecution = new SqlQueryExecution<Long>() {
            @Override
            public String getSqlQuery() {
                DateTime dateTimeNow = new DateTime();
                DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(DATE_FORMAT_STRING);
                LOGGER.info("In getSqlQuery");
                String healthBlockValues = "(" + districtImportRejection.getStateId() + ", " + districtImportRejection.getDistrictCode()+", '"+districtImportRejection.getDistrictName()+",' "  + districtImportRejection.getAccepted() + ", '" + districtImportRejection.getRejectionReason() +"', "+MOTECH_STRING+MOTECH_STRING+MOTECH_STRING+"'"+ dateTimeFormatter.print(dateTimeNow)+"', '"+dateTimeFormatter.print(dateTimeNow)+"')";
                LOGGER.info(healthBlockValues);
                String query = "INSERT into nms_district_rejects (`stateId`, `districtCode`, `districtName`,`accepted`, `rejectionReason`, `creator`, `modifiedBy`, `owner`, `creationDate`, `modificationDate`) VALUES " +
                        healthBlockValues + " ON DUPLICATE KEY UPDATE " +
                        " districtName = VALUES(districtName), accepted = VALUES(accepted), rejectionReason = VALUES(rejectionReason),modificationDate = VALUES(modificationDate), modifiedBy = VALUES(modifiedBy) ";
                LOGGER.info("Printing Query for rejected District: "+ query);
                return query;

            }
            @Override
            public Long execute(Query query) {
                query.setClass(DistrictImportRejection.class);
                LOGGER.info("District class reject query: " + query.toString());
                return (Long) query.execute();
            }

        };
        Long rejectedDistricts = 0L;
        LOGGER.info("Printing District rejection query execution: " + queryExecution.toString());

        rejectedDistricts = districtRejectionDataService.executeSQLQuery(queryExecution);

        return rejectedDistricts;
    }


}
