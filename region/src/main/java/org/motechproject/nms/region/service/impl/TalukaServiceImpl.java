package org.motechproject.nms.region.service.impl;

import org.datanucleus.store.rdbms.query.ForwardQueryResult;
import org.motechproject.mds.query.SqlQueryExecution;
import org.motechproject.nms.region.domain.District;
import org.motechproject.nms.region.domain.Taluka;
import org.motechproject.nms.region.repository.TalukaDataService;
import org.motechproject.nms.region.service.TalukaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.jdo.Query;

@Service("talukaService")
public class TalukaServiceImpl implements TalukaService {

    @Autowired
    private TalukaDataService dataService;

    @Override
    public Taluka findByDistrictAndCode(final District district, final String code) {
        if (district == null) { return null; }

        SqlQueryExecution<Taluka> queryExecution = new SqlQueryExecution<Taluka>() {

            @Override
            public String getSqlQuery() {
                return "select * from nms_talukas where district_id_oid = ? and code = ?";
            }

            @Override
            public Taluka execute(Query query) {
                query.setClass(Taluka.class);
                ForwardQueryResult fqr = (ForwardQueryResult) query.execute(district.getId(), code);
                if (fqr.isEmpty()) {
                    return null;
                }
                if (fqr.size() == 1) {
                    return (Taluka) fqr.get(0);
                }
                throw new IllegalStateException("More than one row returned!");
            }
        };

        return dataService.executeSQLQuery(queryExecution);
    }

    @Override
    public void create(Taluka taluka) {
        dataService.create(taluka);
    }

    @Override
    public void update(Taluka taluka) {
        dataService.update(taluka);
    }
}
