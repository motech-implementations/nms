package org.motechproject.nms.testing.service.impl;

import org.motechproject.alerts.contract.AlertsDataService;
import org.motechproject.mds.query.SqlQueryExecution;
import org.motechproject.nms.csv.repository.CsvAuditRecordDataService;
import org.motechproject.nms.flw.domain.FrontLineWorker;
import org.motechproject.nms.flw.repository.CallContentDataService;
import org.motechproject.nms.flw.repository.CallDetailRecordDataService;
import org.motechproject.nms.flw.repository.FrontLineWorkerDataService;
import org.motechproject.nms.flw.repository.ServiceUsageCapDataService;
import org.motechproject.nms.flw.repository.WhitelistEntryDataService;
import org.motechproject.nms.flw.repository.WhitelistStateDataService;
import org.motechproject.nms.imi.repository.FileAuditRecordDataService;
import org.motechproject.nms.kilkari.domain.Subscription;
import org.motechproject.nms.kilkari.domain.SubscriptionPack;
import org.motechproject.nms.kilkari.domain.SubscriptionPackMessage;
import org.motechproject.nms.kilkari.domain.SubscriptionPackType;
import org.motechproject.nms.kilkari.repository.CallRetryDataService;
import org.motechproject.nms.kilkari.repository.CallSummaryRecordDataService;
import org.motechproject.nms.kilkari.repository.InboxCallDataDataService;
import org.motechproject.nms.kilkari.repository.InboxCallDetailRecordDataService;
import org.motechproject.nms.kilkari.repository.MctsChildDataService;
import org.motechproject.nms.kilkari.repository.MctsMotherDataService;
import org.motechproject.nms.kilkari.repository.SubscriberDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionErrorDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionPackDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionPackMessageDataService;
import org.motechproject.nms.kilkari.service.SubscriptionService;
import org.motechproject.nms.mobileacademy.repository.CompletionRecordDataService;
import org.motechproject.nms.props.repository.DeployedServiceDataService;
import org.motechproject.nms.region.repository.CircleDataService;
import org.motechproject.nms.region.repository.DistrictDataService;
import org.motechproject.nms.region.repository.HealthBlockDataService;
import org.motechproject.nms.region.repository.HealthFacilityDataService;
import org.motechproject.nms.region.repository.HealthFacilityTypeDataService;
import org.motechproject.nms.region.repository.HealthSubFacilityDataService;
import org.motechproject.nms.region.repository.LanguageDataService;
import org.motechproject.nms.region.repository.NationalDefaultLanguageDataService;
import org.motechproject.nms.region.repository.StateDataService;
import org.motechproject.nms.region.repository.TalukaDataService;
import org.motechproject.nms.region.repository.VillageDataService;
import org.motechproject.nms.testing.service.TestingService;
import org.motechproject.server.config.SettingsFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.jdo.Query;
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


    /**
     * FLW
     */
    @Autowired
    private CallContentDataService callContentDataService;
    @Autowired
    private org.motechproject.nms.flw.repository.CallDetailRecordDataService  flwCallDetailRecordDataService;
    @Autowired
    private FrontLineWorkerDataService frontLineWorkerDataService;
    @Autowired
    private ServiceUsageCapDataService serviceUsageCapDataService;
    @Autowired
    private WhitelistEntryDataService whitelistEntryDataService;
    @Autowired
    private WhitelistStateDataService whitelistStateDataService;
    @Autowired
    private CallDetailRecordDataService callDetailRecordDataService;

    /**
     * IMI
     */
    @Autowired
    private FileAuditRecordDataService fileAuditRecordDataService;
    @Autowired
    private org.motechproject.nms.imi.repository.CallDetailRecordDataService imiCallDetailRecordDataService;

    /**
     * Kilkari
     */
    @Autowired
    private CallRetryDataService callRetryDataService;
    @Autowired
    private CallSummaryRecordDataService callSummaryRecordDataService;
    @Autowired
    private InboxCallDataDataService inboxCallDataDataService;
    @Autowired
    private InboxCallDetailRecordDataService inboxCallDetailRecordDataService;
    @Autowired
    private SubscriberDataService subscriberDataService;
    @Autowired
    private SubscriptionService subscriptionService;
    @Autowired
    private SubscriptionPackDataService subscriptionPackDataService;
    @Autowired
    private SubscriptionPackMessageDataService subscriptionPackMessageDataService;
    @Autowired
    private SubscriptionDataService subscriptionDataService;
    @Autowired
    private SubscriptionErrorDataService subscriptionErrorDataService;
    @Autowired
    private MctsMotherDataService mctsMotherDataService;
    @Autowired
    private MctsChildDataService mctsChildDataService;

    /**
     * Mobile Academy
     */
    @Autowired
    private CompletionRecordDataService completionRecordDataService;

    /**
     * Props
     */
    @Autowired
    private DeployedServiceDataService deployedServiceDataService;

    /**
     * Region
     */
    @Autowired
    private CircleDataService circleDataService;
    @Autowired
    private DistrictDataService districtDataService;
    @Autowired
    private HealthBlockDataService healthBlockDataService;
    @Autowired
    private HealthFacilityDataService healthFacilityDataService;
    @Autowired
    private HealthFacilityTypeDataService healthFacilityTypeDataService;
    @Autowired
    private HealthSubFacilityDataService healthSubFacilityDataService;
    @Autowired
    private NationalDefaultLanguageDataService nationalDefaultLanguageLocationDataService;
    @Autowired
    private StateDataService stateDataService;
    @Autowired
    private TalukaDataService talukaDataService;
    @Autowired
    private VillageDataService villageDataService;
    @Autowired
    private LanguageDataService languageDataService;

    /**
     * CSV
     */
    @Autowired
    private CsvAuditRecordDataService csvAuditRecordDataService;


    /**
     * MOTECH Alerts
     */
    @Autowired
    private AlertsDataService alertsDataService;

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


    @Override
    public void clearDatabase() { //NOPMD NcssMethodCount

        LOGGER.debug("clearDatabase()");

        if (!Boolean.parseBoolean(settingsFacade.getProperty(TESTING_ENVIRONMENT))) {
            throw new IllegalStateException("calling clearDatabase() in a production environment is forbidden!");
        }

        /**
         * FLW
         */
        LOGGER.debug("callContentDataService().deleteAll()");
        callContentDataService.deleteAll();
        LOGGER.debug("flwCallDetailRecordDataService().deleteAll()");
        flwCallDetailRecordDataService.deleteAll();

        LOGGER.debug("frontLineWorkerDataService preparing...");
        SqlQueryExecution<List<FrontLineWorker>> flwQueryExecution = new SqlQueryExecution<List<FrontLineWorker>>() {

            @Override
            public String getSqlQuery() {
                return "update nms_front_line_workers set status='INVALID', invalidationDate=null";
            }

            @Override
            public List<FrontLineWorker> execute(Query query) {
                query.execute();
                return null;
            }
        };
        frontLineWorkerDataService.executeSQLQuery(flwQueryExecution);


        LOGGER.debug("serviceUsageCapDataService().deleteAll()");
        serviceUsageCapDataService.deleteAll();
        LOGGER.debug("callDetailRecordDataService().deleteAll()");
        callDetailRecordDataService.deleteAll();
        LOGGER.debug("frontLineWorkerDataService().deleteAll()");
        frontLineWorkerDataService.deleteAll();
        LOGGER.debug("whitelistEntryDataService().deleteAll()");
        whitelistEntryDataService.deleteAll();
        LOGGER.debug("whitelistStateDataService().deleteAll()");
        whitelistStateDataService.deleteAll();

        /**
         * IMI
         */
        LOGGER.debug("fileAuditRecordDataService().deleteAll()");
        fileAuditRecordDataService.deleteAll();
        LOGGER.debug("imiCallDetailRecordDataService().deleteAll()");
        imiCallDetailRecordDataService.deleteAll();

        /**
         * Kilkari
         */
        LOGGER.debug("subscriptionDataService preparing...");
        SqlQueryExecution<List<Subscription>> subscriptionQueryExecution = new SqlQueryExecution<List<Subscription>>() {

            @Override
            public String getSqlQuery() {
                return "update nms_subscriptions set status='COMPLETED', endDate=\" 1970/1/1\"";
            }

            @Override
            public List<Subscription> execute(Query query) {
                query.execute();
                return null;
            }
        };
        subscriptionDataService.executeSQLQuery(subscriptionQueryExecution);

        LOGGER.debug("callRetryDataService().deleteAll()");
        callRetryDataService.deleteAll();
        LOGGER.debug("callSummaryRecordDataService().deleteAll()");
        callSummaryRecordDataService.deleteAll();
        LOGGER.debug("inboxCallDetailRecordDataService().deleteAll()");
        inboxCallDetailRecordDataService.deleteAll();
        LOGGER.debug("inboxCallDataDataService().deleteAll()");
        inboxCallDataDataService.deleteAll();
        LOGGER.debug("subscriberDataService().deleteAll()");
        subscriberDataService.deleteAll();
        LOGGER.debug("subscriptionService().deleteAll()");
        subscriptionService.deleteAll();
        LOGGER.debug("subscriptionPackDataService().deleteAll()");
        subscriptionPackDataService.deleteAll();
        LOGGER.debug("subscriptionPackMessageDataService().deleteAll()");
        subscriptionPackMessageDataService.deleteAll();
        LOGGER.debug("subscriptionErrorDataService().deleteAll()");
        subscriptionErrorDataService.deleteAll();
        LOGGER.debug("callContentDataService().deleteAll()");
        mctsChildDataService.deleteAll();
        LOGGER.debug("mctsMotherDataService().deleteAll()");
        mctsMotherDataService.deleteAll();

        /**
         * Mobile Academy
         */
        LOGGER.debug("completionRecordDataService().deleteAll()");
        completionRecordDataService.deleteAll();

        /**
         * Props
         */
        LOGGER.debug("deployedServiceDataService().deleteAll()");
        deployedServiceDataService.deleteAll();

        /**
         * Region
         */
        LOGGER.debug("circleDataService().deleteAll()");
        circleDataService.deleteAll();
        LOGGER.debug("districtDataService().deleteAll()");
        districtDataService.deleteAll();
        LOGGER.debug("healthBlockDataService().deleteAll()");
        healthBlockDataService.deleteAll();
        LOGGER.debug("healthFacilityDataService().deleteAll()");
        healthFacilityDataService.deleteAll();
        LOGGER.debug("healthFacilityTypeDataService().deleteAll()");
        healthFacilityTypeDataService.deleteAll();
        LOGGER.debug("healthSubFacilityDataService().deleteAll()");
        healthSubFacilityDataService.deleteAll();
        LOGGER.debug("nationalDefaultLanguageLocationDataService().deleteAll()");
        nationalDefaultLanguageLocationDataService.deleteAll();
        LOGGER.debug("languageDataService().deleteAll()");
        languageDataService.deleteAll();
        LOGGER.debug("stateDataService().deleteAll()");
        stateDataService.deleteAll();
        LOGGER.debug("talukaDataService().deleteAll()");
        talukaDataService.deleteAll();
        LOGGER.debug("villageDataService().deleteAll()");
        villageDataService.deleteAll();

        /**
         * Alerts
         */
        LOGGER.debug("alertsDataService().deleteAll()");
        alertsDataService.deleteAll();

        /**
         * CSV
         */
        LOGGER.debug("csvAuditRecordDataService().deleteAll()");
        csvAuditRecordDataService.deleteAll();
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

        if (!Boolean.parseBoolean(settingsFacade.getProperty(TESTING_ENVIRONMENT))) {
            throw new IllegalStateException("calling createSubscriptionPacks() in a production environment is forbidden!");
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
    }
}

