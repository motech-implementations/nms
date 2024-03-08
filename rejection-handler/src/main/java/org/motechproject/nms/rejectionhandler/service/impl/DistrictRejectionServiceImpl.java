package org.motechproject.nms.rejectionhandler.service.impl;

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

    @Autowired
    DistrictRejectionDataService districtRejectionDataService;

    @Override
    @Transactional
    public Long saveRejectedDistrictInBulk(String rejectedDistrictValues) {
        SqlQueryExecution<Long> queryExecution = new SqlQueryExecution<Long>() {
            @Override
            public String getSqlQuery() {
                String query = "INSERT into nms_district_rejects (`stateId`, `districtCode`, `districtName`,`accepted`, `rejectionReason`, `creator`, `modifiedBy`, `owner`, `creationDate`, `modificationDate`, `stateCode`, `mddsCode` ) VALUES " +
                        rejectedDistrictValues + " ON DUPLICATE KEY UPDATE " +
                        " districtName = VALUES(districtName), accepted = VALUES(accepted), rejectionReason = VALUES(rejectionReason),modificationDate = VALUES(modificationDate), modifiedBy = VALUES(modifiedBy), stateCode=VALUES(stateCode), mddsCode=VALUES(mddsCode) ";
                LOGGER.info("Printing Query for rejected District: "+ query);
                return query;

            }
            @Override
            public Long execute(Query query) {
                query.setClass(DistrictImportRejection.class);
                return (Long) query.execute();
            }

        };
        Long rejectedDistricts = 0L;

        if(!rejectedDistrictValues.isEmpty()){
            rejectedDistricts = districtRejectionDataService.executeSQLQuery(queryExecution);
        }

        return rejectedDistricts;
    }

}
