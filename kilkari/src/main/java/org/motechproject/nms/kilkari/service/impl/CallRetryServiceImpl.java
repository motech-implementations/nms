package org.motechproject.nms.kilkari.service.impl;

import org.datanucleus.store.rdbms.query.ForwardQueryResult;
import org.motechproject.mds.query.SqlQueryExecution;
import org.motechproject.metrics.service.Timer;
import org.motechproject.nms.kilkari.domain.CallRetry;
import org.motechproject.nms.kilkari.repository.CallRetryDataService;
import org.motechproject.nms.kilkari.service.CallRetryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.jdo.Query;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service("callRetryService")
public class CallRetryServiceImpl implements CallRetryService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CallRetryServiceImpl.class);

    private CallRetryDataService callRetryDataService;

    @Autowired
    public CallRetryServiceImpl(CallRetryDataService callRetryDataService) {
        this.callRetryDataService = callRetryDataService;
    }


    @Override
    public List<CallRetry> retrieveAll(final long offset, final int max) {
        @SuppressWarnings("unchecked")
        SqlQueryExecution<List<CallRetry>> queryExecution = new SqlQueryExecution<List<CallRetry>>() {

            @Override
            public String getSqlQuery() {
                String query = "SELECT * FROM nms_kk_retry_records WHERE id > :id ORDER BY id LIMIT :limit";
                LOGGER.debug("SQL QUERY: {}", query);
                return query;
            }

            @Override
            public List<CallRetry> execute(Query query) {

                query.setClass(CallRetry.class);

                Map params = new HashMap();
                params.put("id", offset);
                params.put("limit", max);
                ForwardQueryResult fqr = (ForwardQueryResult) query.executeWithMap(params);

                return (List<CallRetry>) fqr;
            }
        };

        return callRetryDataService.executeSQLQuery(queryExecution);
    }
    private String generateNamedParameters(List<Long> specificState) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < specificState.size(); i++) {
            String paramName = ":a" + (i + 1);
            result.append(paramName);

            if (i < specificState.size() - 1) {
                result.append(", ");
            }
        }
        return result.toString();
    }

    @Override
    public List<CallRetry> retrieveAllIVR(long offset, int max, List<Long> specificState) {
        SqlQueryExecution<List<CallRetry>> queryExecution = new SqlQueryExecution<List<CallRetry>>() {

            @Override
            public String getSqlQuery() {
                 String query = "SELECT a.id as id, a.callStage as callStage, a.circle as circle, a.contentFileName as contentFileName, a.languageLocationCode as languageLocationCode, a.msisdn as msisdn, " +
                        "a.subscriptionId as subscriptionId, a.subscriptionOrigin as subscriptionOrigin, a.weekId as weekId, a.creationDate as creationDate, a.creator as creator, a.modificationDate as " +
                        "modificationDate, a.modifiedBy as modifiedBy, a.owner as owner, a.targetFiletimestamp as targetFiletimestamp, a.invalidNumberCount as invalidNumberCount FROM " +
                        "(SELECT r.id, r.callStage, r.circle, r.contentFileName, r.languageLocationCode, r.msisdn, r.subscriptionId, r.subscriptionOrigin, r.weekId, r.creationDate, r.creator, r.modificationDate, r.modifiedBy, r.owner, r.targetFiletimestamp, r.invalidNumberCount,CASE WHEN ss.subscriptionPack_id_OID = 1 THEN s.mother_id_OID WHEN ss.subscriptionPack_id_OID = 2 THEN s.child_id_OID END AS entityId FROM nms_kk_retry_records r " +
                        "INNER JOIN nms_subscriptions ss ON ss.subscriptionId = r.subscriptionId AND ss.status = 'ACTIVE' INNER JOIN nms_subscribers s ON ss.subscriber_id_OID = s.id) a LEFT JOIN nms_mcts_mothers" +
                        " m1 ON a.entityId = m1.id AND m1.state_id_OID IN ( "+generateNamedParameters(specificState) +") LEFT JOIN nms_mcts_children m2 ON a.entityId = m2.id AND m2.state_id_OID IN ( "+generateNamedParameters(specificState)
                        + " ) WHERE (m1.id IS NOT NULL OR " +
                        "m2.id IS NOT NULL)  and a.id > :id ORDER BY id LIMIT :limit";
                /* String query = "SELECT \n" +
                        "    r.id as id, r.callStage as callStage, r.circle as circle, r.contentFileName as contentFileName, \n" +
                        "    r.languageLocationCode as languageLocationCode, r.msisdn as msisdn, r.subscriptionId as subscriptionId, \n" +
                        "    r.subscriptionOrigin as subscriptionOrigin, r.weekId as weekId, r.creationDate as creationDate, \n" +
                        "    r.creator as creator, r.modificationDate as modificationDate, r.modifiedBy as modifiedBy, \n" +
                        "    r.owner as owner, r.targetFiletimestamp as targetFiletimestamp, r.invalidNumberCount as invalidNumberCount " +
                        " FROM nms_kk_retry_records r \n" +
                        "INNER JOIN nms_subscriptions ss ON ss.subscriptionId = r.subscriptionId AND ss.status = 'ACTIVE' \n" +
                        "INNER JOIN nms_subscribers s ON ss.subscriber_id_OID = s.id " +
                        " LEFT JOIN nms_mcts_mothers m1 ON " +
                        "    CASE WHEN ss.subscriptionPack_id_OID = 1 THEN s.mother_id_OID WHEN ss.subscriptionPack_id_OID = 2 THEN s.child_id_OID END = m1.id AND m1.state_id_OID IN (:a1, :a2) " +
                        " LEFT JOIN nms_mcts_children m2 ON \n" +
                        "    CASE WHEN ss.subscriptionPack_id_OID = 1 THEN s.mother_id_OID WHEN ss.subscriptionPack_id_OID = 2 THEN s.child_id_OID END = m2.id AND m2.state_id_OID IN (:a1, :a2) " +
                        " WHERE (m1.id IS NOT NULL OR m2.id IS NOT NULL) AND r.id > :id " +
                        " ORDER BY r.id LIMIT :limit;" ; */
                LOGGER.debug("SQL QUERY: {}", query);
                return query;
            }

            @Override
            public List<CallRetry> execute(Query query) {

                query.setClass(CallRetry.class);

                Map params = new HashMap();
                params.put("id", offset);
                params.put("limit", max);
                 for (int i = 0; i < specificState.size(); i++) {
                    String paramName = "a" + (i + 1);
                    params.put(paramName, specificState.get(i));
                }
                LOGGER.debug("Executing SQL Query: {}", query.toString());
                LOGGER.debug("Parameters: {}", params);
                ForwardQueryResult fqr = (ForwardQueryResult) query.executeWithMap(params);

                return (List<CallRetry>) fqr;
            }
        };

        return callRetryDataService.executeSQLQuery(queryExecution);
    }

    @Override
    public List<CallRetry> retrieveAllNonIVR(long offset, int max, List<Long> specificState) {
        SqlQueryExecution<List<CallRetry>> queryExecution = new SqlQueryExecution<List<CallRetry>>() {

            @Override
            public String getSqlQuery() {
                String query = "SELECT a.id as id, a.callStage as callStage, a.circle as circle, a.contentFileName as contentFileName, a.languageLocationCode as languageLocationCode, a.msisdn as msisdn, " +
                        "a.subscriptionId as subscriptionId, a.subscriptionOrigin as subscriptionOrigin, a.weekId as weekId, a.creationDate as creationDate, a.creator as creator, a.modificationDate as " +
                        "modificationDate, a.modifiedBy as modifiedBy, a.owner as owner, a.targetFiletimestamp as targetFiletimestamp, a.invalidNumberCount as invalidNumberCount FROM " +
                        "(SELECT r.id, r.callStage, r.circle, r.contentFileName, r.languageLocationCode, r.msisdn, r.subscriptionId, r.subscriptionOrigin, r.weekId, r.creationDate, r.creator, r.modificationDate, r.modifiedBy, r.owner, r.targetFiletimestamp, r.invalidNumberCount,CASE WHEN ss.subscriptionPack_id_OID = 1 THEN s.mother_id_OID WHEN ss.subscriptionPack_id_OID = 2 THEN s.child_id_OID END AS entityId FROM nms_kk_retry_records r " +
                        "INNER JOIN nms_subscriptions ss ON ss.subscriptionId = r.subscriptionId AND ss.status = 'ACTIVE' INNER JOIN nms_subscribers s ON ss.subscriber_id_OID = s.id) a LEFT JOIN nms_mcts_mothers" +
                        " m1 ON a.entityId = m1.id AND m1.state_id_OID NOT IN ( "+generateNamedParameters(specificState) +") LEFT JOIN nms_mcts_children m2 ON a.entityId = m2.id AND m2.state_id_OID NOT IN ( "+generateNamedParameters(specificState)
                        + " ) WHERE (m1.id IS NOT NULL OR " +
                        "m2.id IS NOT NULL)  and a.id > :id ORDER BY id LIMIT :limit";
                /* String query = "SELECT \n" +
                        "    r.id as id, r.callStage as callStage, r.circle as circle, r.contentFileName as contentFileName, \n" +
                        "    r.languageLocationCode as languageLocationCode, r.msisdn as msisdn, r.subscriptionId as subscriptionId, \n" +
                        "    r.subscriptionOrigin as subscriptionOrigin, r.weekId as weekId, r.creationDate as creationDate, \n" +
                        "    r.creator as creator, r.modificationDate as modificationDate, r.modifiedBy as modifiedBy, \n" +
                        "    r.owner as owner, r.targetFiletimestamp as targetFiletimestamp, r.invalidNumberCount as invalidNumberCount " +
                        " FROM nms_kk_retry_records r \n" +
                        "INNER JOIN nms_subscriptions ss ON ss.subscriptionId = r.subscriptionId AND ss.status = 'ACTIVE' \n" +
                        "INNER JOIN nms_subscribers s ON ss.subscriber_id_OID = s.id " +
                        " LEFT JOIN nms_mcts_mothers m1 ON " +
                        "    CASE WHEN ss.subscriptionPack_id_OID = 1 THEN s.mother_id_OID WHEN ss.subscriptionPack_id_OID = 2 THEN s.child_id_OID END = m1.id AND m1.state_id_OID NOT IN (:a1, :a2) " +
                        " LEFT JOIN nms_mcts_children m2 ON \n" +
                        "    CASE WHEN ss.subscriptionPack_id_OID = 1 THEN s.mother_id_OID WHEN ss.subscriptionPack_id_OID = 2 THEN s.child_id_OID END = m2.id AND m2.state_id_OID NOT IN (:a1, :a2) " +
                        " WHERE (m1.id IS NOT NULL OR m2.id IS NOT NULL) AND r.id > :id " +
                        " ORDER BY r.id LIMIT :limit;" ; */

                LOGGER.debug("SQL QUERY: {}", query);
                return query;
            }

            @Override
            public List<CallRetry> execute(Query query) {

                query.setClass(CallRetry.class);

                Map params = new HashMap();
                params.put("id", offset);
                params.put("limit", max);
                for (int i = 0; i < specificState.size(); i++) {
                    String paramName = "a" + (i + 1);
                    params.put(paramName, specificState.get(i));
                }
                LOGGER.debug("Executing SQL Query: {}", query.toString());
                LOGGER.debug("Parameters: {}", params);

                ForwardQueryResult fqr = (ForwardQueryResult) query.executeWithMap(params);

                return (List<CallRetry>) fqr;
            }
        };

        return callRetryDataService.executeSQLQuery(queryExecution);
    }


    @Override
    public void deleteOldRetryRecords(final int retentionInDays) {

        @SuppressWarnings("unchecked")
        SqlQueryExecution<Long> queryExecution = new SqlQueryExecution<Long>() {

            @Override
            public String getSqlQuery() {
                String query = "DELETE FROM nms_kk_retry_records where creationDate < now() - INTERVAL :interval DAY";
                LOGGER.debug("SQL QUERY: {}", query);
                return query;
            }

            @Override
            public Long execute(Query query) {

                Map params = new HashMap();
                params.put("interval", retentionInDays);
                return (Long) query.executeWithMap(params);
            }
        };

        LOGGER.debug("Deleting nms_kk_retry_records older than {} days", retentionInDays);
        Timer timer = new Timer();
        long rowCount = callRetryDataService.executeSQLQuery(queryExecution);
        LOGGER.debug("Deleted {} rows from nms_kk_retry_records in {}", rowCount, timer.time());
    }
}
