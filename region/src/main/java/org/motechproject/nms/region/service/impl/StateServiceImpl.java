package org.motechproject.nms.region.service.impl;

import com.google.common.collect.Sets;
import org.datanucleus.store.rdbms.query.ForwardQueryResult;
import org.motechproject.mds.query.SqlQueryExecution;
import org.motechproject.metrics.service.Timer;
import org.motechproject.nms.region.domain.Circle;
import org.motechproject.nms.region.domain.State;
import org.motechproject.nms.region.repository.StateDataService;
import org.motechproject.nms.region.service.StateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.jdo.Query;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.motechproject.nms.region.utils.LocationConstants.CSV_STATE_ID;
import static org.motechproject.nms.region.utils.LocationConstants.OR_SQL_STRING;

@Service("stateService")
public class StateServiceImpl implements StateService {

    private static final Logger LOGGER = LoggerFactory.getLogger(StateServiceImpl.class);

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


    @Override
    public Map<String, State> fillStateIds(List<Map<String, Object>> recordList) {
        final Set<String> stateKeys = new HashSet<>();
        for(Map<String, Object> record : recordList) {
            if (record.get(CSV_STATE_ID) != null) {
                stateKeys.add(record.get(CSV_STATE_ID).toString());
                LOGGER.info("Adding to StateKeys" + record.get(CSV_STATE_ID).toString());
            }
        }

        LOGGER.info("StateKeys Size" + stateKeys.size());

        Map<String, State> stateHashMap = new HashMap<>();
        Timer queryTimer = new Timer();

        @SuppressWarnings("unchecked")
        SqlQueryExecution<List<State>> queryExecution = new SqlQueryExecution<List<State>>() {

            @Override
            public List<State> execute(Query query) {
                query.setClass(State.class);
                ForwardQueryResult fqr = (ForwardQueryResult) query.execute();
                List<State> states;
                if (fqr.isEmpty()) {
                    return null;
                }
                states = (List<State>) fqr;
                return states;
            }

            @Override
            public String getSqlQuery() {
                String query = "SELECT * from nms_states where";
                int count = stateKeys.size();
                LOGGER.info("StateKeys Size " + count);
                for (String stateString : stateKeys) {
                    count--;
                    query += " code = " + stateString;
                    if (count > 0) {
                        query += OR_SQL_STRING;
                    }
                    LOGGER.info("Query " + query);
                }

                LOGGER.info("STATE Query: {}", query);
                return query;
            }


        };

        List<State> states = stateDataService.executeSQLQuery(queryExecution);
        LOGGER.debug("STATE Query time: {}", queryTimer.time());
        for (State state : states) {
            stateHashMap.put(state.getCode().toString(), state);
        }

        return stateHashMap;
    }
}
