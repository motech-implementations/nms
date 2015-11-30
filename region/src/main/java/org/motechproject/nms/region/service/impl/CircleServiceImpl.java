package org.motechproject.nms.region.service.impl;

import com.google.common.collect.Sets;
import org.motechproject.event.MotechEvent;
import org.motechproject.event.listener.EventRelay;
import org.motechproject.event.listener.annotations.MotechListener;
import org.motechproject.mds.query.SqlQueryExecution;
import org.motechproject.nms.region.domain.Circle;
import org.motechproject.nms.region.domain.State;
import org.motechproject.nms.region.repository.CircleDataService;
import org.motechproject.nms.region.service.CircleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.jdo.Query;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service("circleService")
public class CircleServiceImpl implements CircleService {

    public static final String CIRCLE_CACHE_EVICT_MESSAGE = "nms.region.cache.evict.circle";

    @Autowired
    private CircleDataService circleDataService;

    @Autowired
    private EventRelay eventRelay;

    /**
     * Returns the circle for a given name
     *
     * @param name the circle name
     * @return the circle object if found
     */
    @Override
    @Cacheable(value = "circle")
    public Circle getByName(String name) {
        return circleDataService.findByName(name);
    }

    /**
     * Returns all circles in the database
     *
     * @return all the circles in the database
     */
    @Override
    public List<Circle> getAll() {
        return circleDataService.retrieveAll();
    }

    @Override
    public Set<Circle> getAllInState(final State state) {
        SqlQueryExecution<List<Circle>> queryExecution = new SqlQueryExecution<List<Circle>>() {

            @Override
            public String getSqlQuery() {
                String query = "select * " +
                        "from nms_circles c " +
                        "join nms_districts d on d.circle_id_oid = c.id " +
                        "join nms_states s on d.state_id_oid = s.id and s.id = ?";

                return query;
            }

            @Override
            public List<Circle> execute(Query query) {
                query.setClass(Circle.class);
                return (List<Circle>) query.execute(state.getId());
            }
        };

        Set<Circle> circles = Sets.newHashSet(circleDataService.executeSQLQuery(queryExecution));

        if (circles == null) {
            circles = new HashSet<>();
        }

        return circles;
    }


    public void broadcastCacheEvictMessage() {
        eventRelay.broadcastEventMessage(new MotechEvent(CIRCLE_CACHE_EVICT_MESSAGE));
    }


    @MotechListener(subjects = { CIRCLE_CACHE_EVICT_MESSAGE })
    @Transactional
    @CacheEvict(value = { "circle" }, allEntries = true)
    public void cacheEvict(MotechEvent event) {
    }
}
