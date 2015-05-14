package org.motechproject.nms.imi.it;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.alerts.contract.AlertCriteria;
import org.motechproject.alerts.contract.AlertService;
import org.motechproject.alerts.domain.Alert;
import org.motechproject.nms.imi.domain.CallDetailRecord;
import org.motechproject.nms.imi.repository.CallRetryDataService;
import org.motechproject.nms.imi.service.CdrFileService;
import org.motechproject.nms.imi.service.RequestId;
import org.motechproject.nms.imi.service.SettingsService;
import org.motechproject.nms.imi.web.contract.CdrFileNotificationRequest;
import org.motechproject.nms.imi.web.contract.FileInfo;
import org.motechproject.nms.kilkari.domain.DeactivationReason;
import org.motechproject.nms.kilkari.domain.Subscription;
import org.motechproject.nms.kilkari.domain.SubscriptionOrigin;
import org.motechproject.nms.kilkari.domain.SubscriptionStatus;
import org.motechproject.nms.kilkari.repository.SubscriberDataService;
import org.motechproject.nms.kilkari.service.SubscriptionService;
import org.motechproject.nms.props.domain.CallStatus;
import org.motechproject.nms.region.repository.CircleDataService;
import org.motechproject.nms.region.repository.DistrictDataService;
import org.motechproject.nms.region.repository.LanguageDataService;
import org.motechproject.nms.region.repository.LanguageLocationDataService;
import org.motechproject.nms.region.repository.StateDataService;
import org.motechproject.testing.osgi.BasePaxIT;
import org.motechproject.testing.osgi.container.MotechNativeTestContainerFactory;
import org.ops4j.pax.exam.ExamFactory;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;

import javax.inject.Inject;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@ExamFactory(MotechNativeTestContainerFactory.class)
public class CdrFileServiceBundleIT extends BasePaxIT {

    @Inject
    private SettingsService settingsService;

    @Inject
    private SubscriptionService subscriptionService;

    @Inject
    private SubscriberDataService subscriberDataService;

    @Inject
    private LanguageDataService languageDataService;

    @Inject
    private CallRetryDataService callRetryDataService;

    @Inject
    private AlertService alertService;

    @Inject
    CdrFileService cdrFileService;

    @Inject
    private LanguageLocationDataService languageLocationDataService;

    @Inject
    private CircleDataService circleDataService;

    @Inject
    private StateDataService stateDataService;

    @Inject
    private DistrictDataService districtDataService;

    @Before
    public void cleanupDatabase() {
        subscriptionService.deleteAll();
        subscriberDataService.deleteAll();
        languageLocationDataService.deleteAll();
        languageDataService.deleteAll();
        districtDataService.deleteAll();
        stateDataService.deleteAll();
        circleDataService.deleteAll();
        callRetryDataService.deleteAll();
    }


    @Test
    public void testServicePresent() {
        getLogger().debug("testServicePresent()");
        assertTrue(cdrFileService != null);
    }


    @Test
    public void testValidRequest() throws IOException, NoSuchAlgorithmException {
        getLogger().debug("testValidRequest()");

        CdrHelper helper = new CdrHelper(settingsService, subscriptionService, subscriberDataService,
                languageDataService, languageLocationDataService, circleDataService, stateDataService,
                districtDataService, callRetryDataService);

        List<CallDetailRecord> cdrs = helper.makeCdrs();
        helper.setCrds(cdrs);
        helper.makeCdrSummaryFile();
        helper.makeCdrDetailFile();

        cdrFileService.processCdrFile(new CdrFileNotificationRequest(
                        helper.obdFileName(),
                        new FileInfo(helper.cdrSummaryFileName(), helper.summaryFileChecksum(), 7),
                        new FileInfo(helper.cdrDetailFileName(), helper.detailFileChecksum(), 1))
        );

        //todo: look for a more deterministic method than Thread.sleep
        try {
            long sleepyTime = 10 * 1000L;
            getLogger().debug("Sleeping {} seconds to give a chance to @MotechListeners to catch up...", sleepyTime / 1000);
            Thread.sleep(sleepyTime);
            getLogger().debug("...waking up from sleep, did they catch up?");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //Check that the database looks like it should after the CDRs were processed...
        for (CallDetailRecord cdr : cdrs) {
            RequestId requestId = RequestId.fromString(cdr.getRequestId());
            getLogger().debug("Validating CDR processing for {}", requestId.getSubscriptionId());
            Subscription subscription = subscriptionService.getSubscription(requestId.getSubscriptionId());
            if (cdr.getFinalStatus() == CallStatus.SUCCESS) {
                //The call was made, all is good, there should be no record in the CallRetry table
                getLogger().debug("Call was made, no CallRetry record should exist");
                assertNull(callRetryDataService.findBySubscriptionId(requestId.getSubscriptionId()));

                // if this is supposed to be this subscription's last message, check that the subscription is
                // marked as completed
                if (helper.isSubscriptionCompleted(subscription.getSubscriptionId())) {
                    assertEquals(SubscriptionStatus.COMPLETED, subscription.getStatus());
                } else {
                    assertEquals(SubscriptionStatus.ACTIVE, subscription.getStatus());
                }
            } else if (cdr.getFinalStatus() == CallStatus.REJECTED) {
                // The call was rejected, verify the subscription is deactivated and there there is no record in the
                // CallRetry table
                getLogger().debug(
                        "Call was rejected, no CallRetry record should exist and subscription should be deactivated");
                if (subscription.getOrigin() == SubscriptionOrigin.IVR) {
                    // Such CDRs are system failures, check the alerts database contains something about it
                    List<Alert> alerts = alertService.search(new AlertCriteria().byExternalId(
                            subscription.getSubscriptionId()));
                    assertTrue(alerts.size() > 0);
                } else {
                    assertNull(callRetryDataService.findBySubscriptionId(requestId.getSubscriptionId()));
                    assertEquals(DeactivationReason.DO_NOT_DISTURB,
                            subscriptionService.getSubscription(requestId.getSubscriptionId()).getDeactivationReason());
                }
            } else {
                getLogger().debug("Call failed, CallRetry record may exist");

                //The call failed, verify it's in the CallRetry table or not if we exceeded the max retry
                if (helper.shouldRetryCdr(cdr)) {
                    assertNotNull(callRetryDataService.findBySubscriptionId(requestId.getSubscriptionId()));
                } else {
                    assertNull(callRetryDataService.findBySubscriptionId(requestId.getSubscriptionId()));
                }
            }
        }
    }
}
