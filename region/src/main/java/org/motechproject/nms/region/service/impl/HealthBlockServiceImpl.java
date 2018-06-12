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
    // Since Taluka <-> HealthBlocks are many to many, but we don't model that in our system
    // We instead just want to find a taluka with a matching code in the same state as the
    // provided taluka
    public HealthBlock findByTalukaAndCode(final Taluka taluka, final Long code) {
        if (taluka == null) { return null; }

        SqlQueryExecution<HealthBlock> queryExecution = new SqlQueryExecution<HealthBlock>() {

            @Override
            public String getSqlQuery() {
                return "select * " +
                         "from nms_health_blocks " +
                         "join nms_taluka_healthblock j on j.healthblock_id = nms_health_blocks.id " +
                         "join nms_talukas t on j.taluka_id = t.id " +
                         "join nms_districts d on t.district_id_oid = d.id " +
                         "join nms_states s on d.state_id_oid = s.id " +
                         "join nms_states s2 on s.id = s2.id " +
                         "join nms_districts d2 on d2.state_id_oid = s2.id " +
                         "join nms_talukas t2 on t2.district_id_oid = d2.id " +
                        "where nms_health_blocks.code = ? and t2.id = ?";
            }

            @Override
            public HealthBlock execute(Query query) {
                query.setClass(HealthBlock.class);
                ForwardQueryResult fqr = (ForwardQueryResult) query.execute(code, taluka.getId());
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

    @Override
    public HealthBlock create(HealthBlock healthBlock) {
        return healthBlockDataService.create(healthBlock);
    }

    @Override
    public HealthBlock update(HealthBlock healthBlock) {
        return healthBlockDataService.update(healthBlock);
    }
}
