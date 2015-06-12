package org.motechproject.nms.region.service.impl;

import org.datanucleus.store.rdbms.query.ForwardQueryResult;
import org.motechproject.mds.query.SqlQueryExecution;
import org.motechproject.nms.region.domain.HealthBlock;
import org.motechproject.nms.region.domain.Taluka;
import org.motechproject.nms.region.repository.HealthBlockDataService;
import org.motechproject.nms.region.service.HealthBlockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.jdo.Query;

@Service("healthBlockService")
public class HealthBlockServiceImpl implements HealthBlockService {

    @Autowired
    private HealthBlockDataService healthBlockDataService;

    @Override
    public HealthBlock findByTalukaAndCode(final Taluka taluka, final Long code) {

        SqlQueryExecution<HealthBlock> queryExecution = new SqlQueryExecution<HealthBlock>() {

            @Override
            public String getSqlQuery() {
                return "select *  from nms_health_blocks where  taluka_id_oid = ? and code = ?";
            }

            @Override
            public HealthBlock execute(Query query) {
                query.setClass(HealthBlock.class);
                ForwardQueryResult fqr = (ForwardQueryResult) query.execute(taluka.getId(), code);
                if (fqr.isEmpty()) {
                    return null;
                }
                if (fqr.size() == 1) {
                    return (HealthBlock) fqr.get(0);
                }
                throw new IllegalStateException("More than one row returned!");
            }
        };

        return healthBlockDataService.executeSQLQuery(queryExecution);
    }
}
