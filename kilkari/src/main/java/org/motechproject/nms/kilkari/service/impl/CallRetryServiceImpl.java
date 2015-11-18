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
import java.util.List;

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
                String query = String.format("SELECT * FROM nms_kk_retry_records WHERE id > %d ORDER BY id LIMIT %d",
                        offset, max);
                LOGGER.debug("SQL QUERY: {}", query);
                return query;
            }

            @Override
            public List<CallRetry> execute(Query query) {

                query.setClass(CallRetry.class);

                ForwardQueryResult fqr = (ForwardQueryResult) query.execute();

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
                String query = String.format(
                        "DELETE FROM nms_kk_retry_records where creationDate < now() - INTERVAL %d DAY",
                        retentionInDays);
                LOGGER.debug("SQL QUERY: {}", query);
                return query;
            }

            @Override
            public Long execute(Query query) {

                return (Long) query.execute();
            }
        };

        LOGGER.debug("Deleting nms_kk_retry_records older than {} days", retentionInDays);
        Timer timer = new Timer();
        long rowCount = callRetryDataService.executeSQLQuery(queryExecution);
        LOGGER.debug("Deleted {} rows from nms_kk_retry_records in {}", rowCount, timer.time());
    }
}
