package org.motechproject.nms.imi.it;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.nms.imi.domain.CallDetailRecord;
import org.motechproject.nms.imi.service.CdrFileService;
import org.motechproject.nms.imi.service.SettingsService;
import org.motechproject.nms.imi.web.contract.CdrFileNotificationRequest;
import org.motechproject.nms.imi.web.contract.FileInfo;
import org.motechproject.nms.kilkari.repository.SubscriberDataService;
import org.motechproject.nms.kilkari.service.SubscriptionService;
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
    CdrFileService cdrFileService;




    @Test
    public void testServicePresent() {
        assertTrue(cdrFileService != null);
    }


    @Test
    public void testValidRequest() throws IOException, NoSuchAlgorithmException {
        CdrHelper helper = new CdrHelper(settingsService, subscriptionService, subscriberDataService,
                languageDataService, circleLanguageDataService);

        List<CallDetailRecord> cdrs = helper.makeCdrs(5);
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
            getLogger().info("Sleeping {} seconds to give a chance to @MotechListeners to catch up...", sleepyTime/1000);
            Thread.sleep(sleepyTime);
            getLogger().info("...waking up from sleep, did they catch up?");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //todo: check that the database looks like it should after the CDRs were processed...
    }
}
