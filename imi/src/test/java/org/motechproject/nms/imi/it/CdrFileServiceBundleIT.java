package org.motechproject.nms.imi.it;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.nms.imi.domain.CallDetailRecord;
import org.motechproject.nms.imi.repository.CallRetryDataService;
import org.motechproject.nms.imi.service.CdrFileService;
import org.motechproject.nms.imi.service.RequestId;
import org.motechproject.nms.imi.service.SettingsService;
import org.motechproject.nms.imi.web.contract.CdrFileNotificationRequest;
import org.motechproject.nms.imi.web.contract.FileInfo;
import org.motechproject.nms.kilkari.domain.DeactivationReason;
import org.motechproject.nms.kilkari.repository.SubscriberDataService;
import org.motechproject.nms.kilkari.service.SubscriptionService;
import org.motechproject.nms.props.domain.CallStatus;
import org.motechproject.nms.region.language.repository.CircleLanguageDataService;
import org.motechproject.nms.region.language.repository.LanguageDataService;
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

import static junit.framework.Assert.*;
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
    private CircleLanguageDataService circleLanguageDataService;

    @Inject
    private CallRetryDataService callRetryDataService;

    @Inject
    CdrFileService cdrFileService;


    @Before
    public void cleanupDatabase() {
        subscriptionService.deleteAll();
        subscriberDataService.deleteAll();
        circleLanguageDataService.deleteAll();
        languageDataService.deleteAll();
        callRetryDataService.deleteAll();
    }


    @Test
    public void testServicePresent() {
        getLogger().debug("testServicePresent()");
        assertTrue(cdrFileService != null);
    }


    @Ignore // TODO: fhuster to renable once passing
    @Test
    public void testValidRequest() throws IOException, NoSuchAlgorithmException {
        getLogger().debug("testValidRequest()");

        CdrHelper helper = new CdrHelper(settingsService, subscriptionService, subscriberDataService,
                languageDataService, circleLanguageDataService);

        List<CallDetailRecord> cdrs = helper.makeCdrs();
        helper.setCrds(cdrs);
        helper.makeCdrSummaryFile();
        helper.makeCdrDetailFile();

        cdrFileService.processCdrFile(new CdrFileNotificationRequest(
                        helper.obdFileName(),
                        new FileInfo(helper.cdrSummaryFileName(), helper.summaryFileChecksum(), 0),
                        new FileInfo(helper.cdrDetailFileName(), helper.detailFileChecksum(), 1))
        );

        try {
            long sleepyTime = 10 * 1000L;
            getLogger().debug("Sleeping {} seconds to give a chance to @MotechListeners to catch up...", sleepyTime/1000);
            Thread.sleep(sleepyTime);
            getLogger().debug("...waking up from sleep, did they catch up?");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //Check that the database looks like it should after the CDRs were processed...
        for (CallDetailRecord cdr : cdrs) {
            RequestId requestId = RequestId.fromString(cdr.getRequestId());
            getLogger().debug("Validating CDR processing for {}", requestId.getSubscriptionId());
            if (cdr.getFinalStatus() == CallStatus.SUCCESS) {
                //The call was made, all is good, there should be no record in the CallRetry table
                getLogger().debug("Call was made, no CallRetry record should exist");
                assertNull(callRetryDataService.findBySubscriptionId(requestId.getSubscriptionId()));
            } else if (cdr.getFinalStatus() == CallStatus.REJECTED) {
                // The call was rejected, verify the suvbscription is deactivated and there there is no record in the
                // CallRetry table
                getLogger().debug(
                        "Call was rejected, no CallRetry record should exist and subscription should be deactivated");
                assertNull(callRetryDataService.findBySubscriptionId(requestId.getSubscriptionId()));
                assertEquals(DeactivationReason.DO_NOT_DISTURB,
                        subscriptionService.getSubscription(requestId.getSubscriptionId()).getDeactivationReason());
            } else {
                getLogger().debug("Call failed, CallRetry record should exist");
                //The call failed, verify it's in the CallRetry table
                //todo: make that more complex and include the possibility of having exhausted the max number of retries
                assertNotNull(callRetryDataService.findBySubscriptionId(requestId.getSubscriptionId()));
            }
        }
    }
}
