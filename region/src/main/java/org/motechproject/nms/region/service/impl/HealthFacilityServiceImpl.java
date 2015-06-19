package org.motechproject.nms.region.service.impl;

import org.datanucleus.store.rdbms.query.ForwardQueryResult;
import org.motechproject.mds.query.SqlQueryExecution;
import org.motechproject.nms.region.domain.HealthBlock;
import org.motechproject.nms.region.domain.HealthFacility;
import org.motechproject.nms.region.repository.HealthFacilityDataService;
import org.motechproject.nms.region.service.HealthFacilityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.jdo.Query;

@Service("healthFacilityService")
public class HealthFacilityServiceImpl implements HealthFacilityService {
    @Autowired
    private HealthFacilityDataService dataService;

    @Override
    public HealthFacility findByHealthBlockAndCode(final HealthBlock healthBlock, final Long code) {

        SqlQueryExecution<HealthFacility> queryExecution = new SqlQueryExecution<HealthFacility>() {

            @Override
            public String getSqlQuery() {
                return "select *  from nms_health_facilities where healthBlock_id_oid = ? and code = ?";
            }

            @Override
            public HealthFacility execute(Query query) {
                query.setClass(HealthFacility.class);
                ForwardQueryResult fqr = (ForwardQueryResult) query.execute(healthBlock.getId(), code);
                if (fqr.isEmpty()) {
                    return null;
                }
                if (fqr.size() == 1) {
                    return (HealthFacility) fqr.get(0);
                }
                throw new IllegalStateException("More than one row returned!");
            }
        };

        return dataService.executeSQLQuery(queryExecution);
    }
}
