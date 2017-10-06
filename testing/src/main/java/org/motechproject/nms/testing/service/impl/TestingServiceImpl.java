package org.motechproject.nms.testing.service.impl;

import org.motechproject.mds.query.SqlQueryExecution;
import org.motechproject.metrics.service.Timer;
import org.motechproject.nms.kilkari.domain.SubscriptionPack;
import org.motechproject.nms.kilkari.domain.SubscriptionPackMessage;
import org.motechproject.nms.kilkari.domain.SubscriptionPackType;
import org.motechproject.nms.kilkari.repository.SubscriptionPackDataService;
import org.motechproject.nms.kilkari.service.SubscriptionService;
import org.motechproject.nms.region.repository.DistrictDataService;
import org.motechproject.nms.region.repository.StateDataService;
import org.motechproject.nms.region.service.LanguageService;
import org.motechproject.nms.testing.service.TestingService;
import org.motechproject.server.config.SettingsFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.jdo.Query;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service("testingService")
public class TestingServiceImpl implements TestingService {

    private static final String TESTING_ENVIRONMENT = "testing.environment";
    private static final int PREGNANCY_PACK_WEEKS = 72;
    private static final int CHILD_PACK_WEEKS = 48;
    private static final int TWO_MINUTES = 120;
    private static final int TEN_SECS = 10;

    private static final Logger LOGGER = LoggerFactory.getLogger(TestingServiceImpl.class);
    public static final String CHILD_PACK = "childPack";
    public static final String PREGNANCY_PACK = "pregnancyPack";
    public static final String TESTING_SERVICE_FORBIDDEN = "calling TestingService in a production environment is forbidden!";


    private static final String[] QUERIES = {
    };

    private static final String[] TABLES = {
        "ALERTS_MODULE_ALERT",
        "ALERTS_MODULE_ALERT_DATA",
        "ALERTS_MODULE_ALERT__TRASH",
        "ALERTS_MODULE_ALERT__TRASH_DATA",
        "MTRAINING_MODULE_ACTIVITYRECORD",
        "MTRAINING_MODULE_ACTIVITYRECORD__TRASH",
        "MTRAINING_MODULE_ACTIVITYRECORD_Audit",
        "MTRAINING_MODULE_ACTIVITYRECORD_Audit__TRASH",
        "MTRAINING_MODULE_BOOKMARK",
        "MTRAINING_MODULE_BOOKMARK__TRASH",
        "MTRAINING_MODULE_CHAPTER",
        "MTRAINING_MODULE_CHAPTER__TRASH",
        "MTRAINING_MODULE_COURSE",
        "MTRAINING_MODULE_COURSEUNITMETADATA",
        "MTRAINING_MODULE_COURSEUNITMETADATA__TRASH",
        "MTRAINING_MODULE_COURSE__TRASH",
        "MTRAINING_MODULE_LESSON",
        "MTRAINING_MODULE_LESSON__TRASH",
        "MTRAINING_MODULE_QUESTION",
        "MTRAINING_MODULE_QUESTION__TRASH",
        "MTRAINING_MODULE_QUIZ",
        "MTRAINING_MODULE_QUIZ__TRASH",
        "nms_flw_status_update_audit",
        "nms_flw_status_update_audit__TRASH",
        "nms_anonymous_call_details_audit",
        "nms_anonymous_call_details_audit__TRASH",
        "nms_inactive_job_call_audit",
        "nms_inactive_job_call_audit__TRASH",
        "nms_call_content",
        "nms_call_content__TRASH",
        "nms_subscription_errors",
        "nms_subscription_errors__TRASH",
        "nms_subscription_pack_messages",
        "nms_subscription_pack_messages__TRASH",
        "nms_subscriptions",
        "nms_subscriptions__TRASH",
        "nms_subscription_packs",
        "nms_subscription_packs__TRASH",
        "nms_subscribers",
        "nms_subscribers__TRASH",
        "nms_subscriber_msisdn_tracker",
        "nms_subscriber_msisdn_tracker__TRASH",
        "nms_circles",
        "nms_circles__TRASH",
        "nms_contactNumber_audit",
        "nms_contactNumber_audit__TRASH",
        "nms_csv_audit_records",
        "nms_csv_audit_records__TRASH",
        "nms_deployed_services",
        "nms_deployed_services__TRASH",
        "nms_flw_cdrs",
        "nms_flw_cdrs__TRASH",
        "nms_front_line_workers",
        "nms_front_line_workers__TRASH",
        "nms_districts",
        "nms_districts__TRASH",
        "nms_flw_errors",
        "nms_health_blocks",
        "nms_health_blocks__TRASH",
        "nms_health_facilities",
        "nms_health_facilities__TRASH",
        "nms_health_facility_types",
        "nms_health_facility_types__TRASH",
        "nms_health_sub_facilities",
        "nms_health_sub_facilities__TRASH",
        "nms_imi_cdrs",
        "nms_imi_cdrs__TRASH",
        "nms_imi_csrs",
        "nms_imi_csrs__TRASH",
        "nms_imi_file_audit_records",
        "nms_imi_file_audit_records__TRASH",
        "nms_inbox_call_data",
        "nms_inbox_call_data__TRASH",
        "nms_inbox_call_details",
        "nms_inbox_call_details__TRASH",
        "nms_kk_retry_records",
        "nms_kk_retry_records__TRASH",
        "nms_ma_course_completion_records",
        "nms_ma_course_completion_records__TRASH",
        "nms_ma_course",
        "nms_ma_course__TRASH",
        "nms_mcts_audit",
        "nms_mcts_audit__TRASH",
        "nms_mcts_beneficiaries__TRASH",
        "nms_mcts_children",
        "nms_mcts_children__TRASH",
        "nms_mcts_failures",
        "nms_mcts_failures__TRASH",
        "nms_mcts_mothers",
        "nms_mcts_mothers__TRASH",
        "nms_national_default_language",
        "nms_national_default_language__TRASH",
        "nms_service_usage_caps",
        "nms_service_usage_caps__TRASH",
        "nms_states",
        "nms_states__TRASH",
        "nms_languages",
        "nms_languages__TRASH",
        "nms_talukas",
        "nms_talukas__TRASH",
        "nms_villages",
        "nms_villages__TRASH",
        "nms_whitelist_entries",
        "nms_whitelist_entries__TRASH",
        "nms_whitelisted_states",
        "nms_whitelisted_states__TRASH",
        "nms_blocked_msisdn",
        "nms_blocked_msisdn__TRASH",
        "nms_deactivation_subscription_audit_records",
        "nms_deactivation_subscription_audit_records__TRASH",
        "TRACKING_MODULE_CHANGELOG",
        "TRACKING_MODULE_CHANGELOG__TRASH",
        "nms_deactivated_beneficiary",
        "nms_deactivated_beneficiary__TRASH",
        "nms_flw_rejects",
        "nms_flw_rejects__TRASH",
        "nms_child_rejects",
        "nms_child_rejects__TRASH",
        "nms_mother_rejects",
        "nms_mother_rejects__TRASH",
        "nms_rch_import_facilitator",
        "nms_rch_import_facilitator__TRASH"
    };

    /**
     * Kilkari
     */
    @Autowired
    private SubscriptionService subscriptionService;
    @Autowired
    private SubscriptionPackDataService subscriptionPackDataService;

    /**
     * Region
     */
    @Autowired
    private DistrictDataService districtDataService;
    @Autowired
    private LanguageService languageService;
    @Autowired
    private StateDataService stateDataService;


    /**
     * SettingsFacade
     */
    @Autowired
    @Qualifier("testingSettings")
    private SettingsFacade settingsFacade;


    public TestingServiceImpl() {
        //
        // Should only happen on dev / CI machines, so no need to save/restore settings
        //
        System.setProperty("org.motechproject.testing.osgi.http.numTries", "1");
    }


    private MctsHelper createMctsHelper() {
        return new MctsHelper(settingsFacade, stateDataService, districtDataService);
    }


    private void changeConstraints(final boolean disable) {
        SqlQueryExecution sqe = new SqlQueryExecution() {

            @Override
            public String getSqlQuery() {
                return String.format("SET FOREIGN_KEY_CHECKS = %d", disable ? 0 : 1);
            }

            @Override
            public Object execute(Query query) {
                query.execute();
                return null;
            }
        };
        stateDataService.executeSQLQuery(sqe);
    }


    private void disableConstraints() {
        changeConstraints(true);
    }


    private void enableConstraints() {
        changeConstraints(false);
    }


    private void execQuery(final String sqlQuery) {
        SqlQueryExecution sqe = new SqlQueryExecution() {

            @Override
            public String getSqlQuery() {
                return sqlQuery;
            }

            @Override
            public Object execute(Query query) {
                query.execute();
                return null;
            }
        };
        try {
            stateDataService.executeSQLQuery(sqe);
        } catch (Exception e) {
            String s = String.format("Exception While executing \"%s\"", sqlQuery);
            LOGGER.error(s);
            throw e;
        }
    }


    private void truncateTable(final String table) {
        SqlQueryExecution sqe = new SqlQueryExecution() {

            @Override
            public String getSqlQuery() {
                return String.format("DELETE FROM %s WHERE 1=1", table);
            }

            @Override
            public Object execute(Query query) {
                query.execute();
                return null;
            }
        };
        try {
            stateDataService.executeSQLQuery(sqe);
        } catch (Exception e) {
            String s = String.format("Exception while deleting %s : %s", table, e.getMessage());
            LOGGER.error(s);
            throw e;
        }
    }


    @Override
    public void clearDatabase() {
        Timer timer = new Timer();

        if (!Boolean.parseBoolean(settingsFacade.getProperty(TESTING_ENVIRONMENT))) {
            throw new IllegalStateException(TESTING_SERVICE_FORBIDDEN);
        }

        for (String query : QUERIES) {
            execQuery(query);
        }

        disableConstraints();

        for (String table : TABLES) {
            truncateTable(table);
        }

        enableConstraints();

        languageService.cacheEvict(null);

        LOGGER.debug("clearDatabase: {}", timer.time());
    }


    public SubscriptionPack childPack() {
        if (subscriptionPackDataService.byName(CHILD_PACK) == null) {
            createSubscriptionPack(CHILD_PACK, SubscriptionPackType.CHILD, CHILD_PACK_WEEKS, 1);
        }
        return subscriptionService.getSubscriptionPack(CHILD_PACK);
    }


    public SubscriptionPack pregnancyPack() {
        if (subscriptionPackDataService.byName(PREGNANCY_PACK) == null) {
            createSubscriptionPack(PREGNANCY_PACK, SubscriptionPackType.PREGNANCY, PREGNANCY_PACK_WEEKS, 2);
        }
        return subscriptionService.getSubscriptionPack(PREGNANCY_PACK);
    }


    private void createSubscriptionPack(String name, SubscriptionPackType type, int weeks,
                                        int messagesPerWeek) {
        List<SubscriptionPackMessage> messages = genratePackMessageList(weeks, messagesPerWeek);
        subscriptionPackDataService.create(new SubscriptionPack(name, type, weeks, messagesPerWeek, messages));
    }


    private List<SubscriptionPackMessage> genratePackMessageList(int packWeeks, int messagesPerWeek) {
        List<SubscriptionPackMessage> messages = new ArrayList<>();
        for (int week = 1; week <= packWeeks; week++) {
            messages.add(new SubscriptionPackMessage(String.format("w%s_1", week),
                    String.format("w%s_1.wav", week),
                    TWO_MINUTES - TEN_SECS + (int) (Math.random() * 2 * TEN_SECS)));

            if (messagesPerWeek == 2) {
                messages.add(new SubscriptionPackMessage(String.format("w%s_2", week),
                        String.format("w%s_2.wav", week),
                        TWO_MINUTES - TEN_SECS + (int) (Math.random() * 2 * TEN_SECS)));
            }
        }
        return messages;
    }


    @Override
    public void createSubscriptionPacks() {

        LOGGER.debug("createSubscriptionPacks()");

        Timer timer = new Timer();

        if (!Boolean.parseBoolean(settingsFacade.getProperty(TESTING_ENVIRONMENT))) {
            throw new IllegalStateException(TESTING_SERVICE_FORBIDDEN);
        }

        subscriptionPackDataService.create(
                new SubscriptionPack(
                        CHILD_PACK,
                        SubscriptionPackType.CHILD,
                        CHILD_PACK_WEEKS,
                        1,
                        genratePackMessageList(CHILD_PACK_WEEKS, 1)
                )
        );
        subscriptionPackDataService.create(
                new SubscriptionPack(
                        PREGNANCY_PACK,
                        SubscriptionPackType.PREGNANCY,
                        PREGNANCY_PACK_WEEKS,
                        2,
                        genratePackMessageList(PREGNANCY_PACK_WEEKS, 2)
                )
        );

        LOGGER.debug("createSubscriptionPacks: {}", timer.time());
    }


    @Override
    public String createMctsMoms(int count, boolean staticLMP) throws IOException {

        LOGGER.debug("createMctsMoms(count={}, lmp={})", count, staticLMP);

        if (!Boolean.parseBoolean(settingsFacade.getProperty(TESTING_ENVIRONMENT))) {
            throw new IllegalStateException(TESTING_SERVICE_FORBIDDEN);
        }

        MctsHelper mctsHelper = createMctsHelper();
        return mctsHelper.createMoms(count, staticLMP);
    }


    @Override
    public String createMctsKids(int count, boolean staticDOB) throws IOException {

        LOGGER.debug("createMctsKids(count={}, dob={})", count, staticDOB);

        if (!Boolean.parseBoolean(settingsFacade.getProperty(TESTING_ENVIRONMENT))) {
            throw new IllegalStateException(TESTING_SERVICE_FORBIDDEN);
        }

        MctsHelper mctsHelper = createMctsHelper();
        return mctsHelper.createKids(count, staticDOB);
    }
}
