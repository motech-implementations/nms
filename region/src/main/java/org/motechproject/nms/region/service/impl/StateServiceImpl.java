package org.motechproject.nms.region.service.impl;

import com.google.common.collect.Sets;
import org.motechproject.mds.query.SqlQueryExecution;
import org.motechproject.nms.region.domain.Circle;
import org.motechproject.nms.region.domain.State;
import org.motechproject.nms.region.repository.StateDataService;
import org.motechproject.nms.region.service.StateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.jdo.Query;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service("stateService")
public class StateServiceImpl implements StateService {
    @Autowired
    private StateDataService stateDataService;

    @Override
    public Set<State> getAllInCircle(final Circle circle) {
        SqlQueryExecution<List<State>> queryExecution = new SqlQueryExecution<List<State>>() {

            @Override
            public String getSqlQuery() {
                String query = "select * " +
                        "from nms_states s " +
                        "join nms_districts d on d.state_id_oid = s.id " +
                        "join nms_circles c on d.circle_id_oid = c.id and c.id = ?";

                return query;
            }

            @Override
            public List<State> execute(Query query) {
                query.setClass(State.class);
                return (List<State>) query.execute(circle.getId());
            }
        };

        Set<State> states = Sets.newHashSet(stateDataService.executeSQLQuery(queryExecution));

        if (states == null) {
            states = new HashSet<>();
        }

        return states;
    }
}
