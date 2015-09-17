package org.motechproject.nms.region.service.impl;

import org.datanucleus.store.rdbms.query.ForwardQueryResult;
import org.motechproject.mds.query.QueryExecution;
import org.motechproject.mds.query.SqlQueryExecution;
import org.motechproject.mds.util.InstanceSecurityRestriction;
import org.motechproject.nms.region.domain.District;
import org.motechproject.nms.region.domain.Language;
import org.motechproject.nms.region.domain.State;
import org.motechproject.nms.region.repository.DistrictDataService;
import org.motechproject.nms.region.service.DistrictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.jdo.Query;
import java.util.HashSet;
import java.util.Set;

@Service("districtService")
public class DistrictServiceImpl implements DistrictService {

    private DistrictDataService districtDataService;

    @Autowired
    public DistrictServiceImpl(DistrictDataService districtDataService) {
        this.districtDataService = districtDataService;
    }

    @Override
    @Cacheable("district-language")
    public Set<District> getAllForLanguage(final Language language) {

        QueryExecution<Set<District>> stateQueryExecution = new QueryExecution<Set<District>>() {
            @Override
            public Set<District> execute(Query query, InstanceSecurityRestriction restriction) {

                query.setFilter("language == _language");
                query.declareParameters("org.motechproject.nms.region.domain.Language _language");

                Set<District> districts = new HashSet<>();
                ForwardQueryResult fqr = (ForwardQueryResult) query.execute(language);
                for (Object o : fqr) {
                    districts.add((District) o);
                }
                return districts;
            }
        };

        return districtDataService.executeQuery(stateQueryExecution);
    }

    @Override
    @Cacheable("district-state-code")
    public District findByStateAndCode(final State state, final Long code) {
        if (state == null) { return null; }

        SqlQueryExecution<District> queryExecution = new SqlQueryExecution<District>() {

            @Override
            public String getSqlQuery() {
                return "select * from nms_districts where state_id_oid = ? and code = ?";
            }

            @Override
            public District execute(Query query) {
                query.setClass(District.class);
                ForwardQueryResult fqr = (ForwardQueryResult) query.execute(state.getId(), code);
                if (fqr.isEmpty()) {
                    return null;
                }
                if (fqr.size() == 1) {
                    return (District) fqr.get(0);
                }
                throw new IllegalStateException("More than one row returned!");
            }
        };

        return districtDataService.executeSQLQuery(queryExecution);

    }

    @Override
    @Cacheable("district-state-name")
    public District findByStateAndName(final State state, final String name) {
        if (state == null) { return null; }

        SqlQueryExecution<District> queryExecution = new SqlQueryExecution<District>() {

            @Override
            public String getSqlQuery() {
                return "select * from nms_districts where state_id_oid = ? and name = ?";
            }

            @Override
            public District execute(Query query) {
                query.setClass(District.class);
                ForwardQueryResult fqr = (ForwardQueryResult) query.execute(state.getId(), name);
                if (fqr.isEmpty()) {
                    return null;
                }
                if (fqr.size() == 1) {
                    return (District) fqr.get(0);
                }
                throw new IllegalStateException("More than one row returned!");
            }
        };

        return districtDataService.executeSQLQuery(queryExecution);

    }

    @Override
    public void create(District district) {
        districtDataService.create(district);
    }

    @Override
    public void update(District district) {
        districtDataService.update(district);
    }

}
