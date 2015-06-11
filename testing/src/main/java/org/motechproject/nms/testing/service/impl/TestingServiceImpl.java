package org.motechproject.nms.testing.service.impl;

import org.joda.time.DateTime;
import org.joda.time.Weeks;
import org.motechproject.alerts.contract.AlertsDataService;
import org.motechproject.nms.flw.domain.FrontLineWorker;
import org.motechproject.nms.flw.domain.FrontLineWorkerStatus;
import org.motechproject.nms.flw.repository.CallContentDataService;
import org.motechproject.nms.flw.repository.CallDetailRecordDataService;
import org.motechproject.nms.flw.repository.FrontLineWorkerDataService;
import org.motechproject.nms.flw.repository.ServiceUsageCapDataService;
import org.motechproject.nms.flw.repository.ServiceUsageDataService;
import org.motechproject.nms.flw.repository.WhitelistEntryDataService;
import org.motechproject.nms.flw.repository.WhitelistStateDataService;
import org.motechproject.nms.imi.repository.FileAuditRecordDataService;
import org.motechproject.nms.kilkari.domain.Subscription;
import org.motechproject.nms.kilkari.domain.SubscriptionStatus;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("testingService")
public class TestingServiceImpl implements TestingService {

    private static final String TESTING_ENVIRONMENT = "testing.environment";
    private static final String WEEKS_TO_KEEP_INVALID_FLWS = "flw.weeks_to_keep_invalid_flws";

    /**
     * FLW
     */
    @Autowired
    private CallContentDataService callContentDataService;
    @Autowired
    private CallDetailRecordDataService callDetailRecordDataService;
    @Autowired
    private FrontLineWorkerDataService frontLineWorkerDataService;
    @Autowired
    private ServiceUsageCapDataService serviceUsageCapDataService;
    @Autowired
    private ServiceUsageDataService serviceUsageDataService;
    @Autowired
    private WhitelistEntryDataService whitelistEntryDataService;
    @Autowired
    private WhitelistStateDataService whitelistStateDataService;

    /**
     * IMI
     */
    @Autowired
    private FileAuditRecordDataService fileAuditRecordDataService;

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
    public void clearDatabase() {

        if (!Boolean.parseBoolean(settingsFacade.getProperty(TESTING_ENVIRONMENT))) {
            throw new IllegalStateException("calling clearDatabase() in a production environment is forbidden!");
        }

        /**
         * FLW
         */
        callContentDataService.deleteAll();
        callDetailRecordDataService.deleteAll();
        int weeks = Integer.parseInt(settingsFacade.getProperty(WEEKS_TO_KEEP_INVALID_FLWS));
        DateTime now = DateTime.now();
        for (FrontLineWorker flw: frontLineWorkerDataService.retrieveAll()) {
            if ((flw.getStatus() != FrontLineWorkerStatus.INVALID) ||
                    (flw.getInvalidationDate() == null) ||
                    (Math.abs(Weeks.weeksBetween(now, flw.getInvalidationDate()).getWeeks()) < weeks)) {
                flw.setStatus(FrontLineWorkerStatus.INVALID);
                flw.setInvalidationDate(DateTime.now().minusYears(1));
                frontLineWorkerDataService.update(flw);
            }
        }
        serviceUsageCapDataService.deleteAll();
        serviceUsageDataService.deleteAll();
        frontLineWorkerDataService.deleteAll();
        whitelistEntryDataService.deleteAll();
        whitelistStateDataService.deleteAll();


        /**
         * IMI
         */
        fileAuditRecordDataService.deleteAll();


        /**
         * Kilkari
         */
        for (Subscription subscription: subscriptionDataService.retrieveAll()) {
            try {
                subscriptionService.deletePreconditionCheck(subscription);
            } catch (IllegalStateException e) {
                subscription.setStatus(SubscriptionStatus.COMPLETED);
                subscription.setEndDate(DateTime.now().minusYears(1));
                subscriptionDataService.update(subscription);
            }
        }
        callRetryDataService.deleteAll();
        callSummaryRecordDataService.deleteAll();
        inboxCallDetailRecordDataService.deleteAll();
        inboxCallDataDataService.deleteAll();
        subscriberDataService.deleteAll();
        subscriptionService.deleteAll();
        subscriptionPackDataService.deleteAll();
        subscriptionPackMessageDataService.deleteAll();
        subscriptionErrorDataService.deleteAll();
        mctsChildDataService.deleteAll();
        mctsMotherDataService.deleteAll();

        /**
         * Mobile Academy
         */
        completionRecordDataService.deleteAll();


        /**
         * Props
         */
        deployedServiceDataService.deleteAll();


        /**
         * Region
         */
        circleDataService.deleteAll();
        districtDataService.deleteAll();
        healthBlockDataService.deleteAll();
        healthFacilityDataService.deleteAll();
        healthFacilityTypeDataService.deleteAll();
        healthSubFacilityDataService.deleteAll();
        nationalDefaultLanguageLocationDataService.deleteAll();
        languageDataService.deleteAll();
        stateDataService.deleteAll();
        talukaDataService.deleteAll();
        villageDataService.deleteAll();


        /**
         * Alerts
         */
        alertsDataService.deleteAll();
    }

}

