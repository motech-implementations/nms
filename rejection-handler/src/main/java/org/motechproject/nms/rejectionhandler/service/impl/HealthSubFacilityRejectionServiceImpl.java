package org.motechproject.nms.rejectionhandler.service.impl;

import org.motechproject.mds.query.SqlQueryExecution;
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

    @Autowired
    private HealthSubFacilityRejectionDataService healthSubFacilityRejectionDataService;

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
