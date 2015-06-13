package org.motechproject.nms.testing.it.kilkari;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.motechproject.nms.kilkari.domain.MctsChild;
import org.motechproject.nms.kilkari.domain.Subscriber;
import org.motechproject.nms.kilkari.domain.Subscription;
import org.motechproject.nms.kilkari.domain.SubscriptionOrigin;
import org.motechproject.nms.kilkari.domain.SubscriptionPackType;
import org.motechproject.nms.kilkari.repository.SubscriberDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionPackDataService;
import org.motechproject.nms.kilkari.service.MctsBeneficiaryUpdateService;
import org.motechproject.nms.kilkari.service.SubscriptionService;
import org.motechproject.nms.region.repository.CircleDataService;
import org.motechproject.nms.region.repository.DistrictDataService;
import org.motechproject.nms.region.repository.LanguageDataService;
import org.motechproject.nms.region.repository.StateDataService;
import org.motechproject.nms.testing.it.utils.SubscriptionHelper;
import org.motechproject.nms.testing.service.TestingService;
import org.motechproject.testing.osgi.BasePaxIT;
import org.motechproject.testing.osgi.container.MotechNativeTestContainerFactory;
import org.ops4j.pax.exam.ExamFactory;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;

import javax.inject.Inject;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;


@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@ExamFactory(MotechNativeTestContainerFactory.class)
public class MctsBeneficiaryUpdateServiceBundleIT extends BasePaxIT {


    @Inject
    private TestingService testingService;
    @Inject
    private MctsBeneficiaryUpdateService mctsBeneficiaryUpdateService;
    @Inject
    private LanguageDataService languageDataService;
    @Inject
    private StateDataService stateDataService;
    @Inject
    private DistrictDataService districtDataService;
    @Inject
    private CircleDataService circleDataService;
    @Inject
    private SubscriberDataService subscriberDataService;
    @Inject
    private SubscriptionService subscriptionService;
    @Inject
    private SubscriptionPackDataService subscriptionPackDataService;

    private SubscriptionHelper subscriptionHelper;

    @Before
    public void setUp() {
        testingService.clearDatabase();

        subscriptionHelper = new SubscriptionHelper(subscriptionService, subscriberDataService,
                subscriptionPackDataService, languageDataService, circleDataService,
                stateDataService, districtDataService);
        subscriptionHelper.pregnancyPack();
        subscriptionHelper.childPack();
    }

    public void testUpdateMsisdn() throws Exception {
        Long oldMsisdn = subscriptionHelper.makeNumber();
        Long newMsisdn = subscriptionHelper.makeNumber();

        Subscription subscription = subscriptionHelper.mksub(SubscriptionOrigin.MCTS_IMPORT, DateTime.now(), SubscriptionPackType.CHILD, oldMsisdn);
        String mctsId = "0123456789";

        subscription.getSubscriber().setChild(new MctsChild(mctsId));

        Reader reader = createMsisdnReaderWithHeaders(mctsId + "," + newMsisdn);
        mctsBeneficiaryUpdateService.updateMsisdn(reader);

        assertNull(subscriberDataService.findByCallingNumber(oldMsisdn));

        Subscriber subscriber = subscriberDataService.findByCallingNumber(newMsisdn);
        assertNotNull(subscriber);
        assertEquals(mctsId, subscriber.getChild().getBeneficiaryId());
    }

    public void testUpdateMsisdnForSubscriberWithBothPacks() throws Exception {

    }


    public void testUpdateMsisdnNumberAlreadyInUse() throws Exception {

    }



    private Reader createMsisdnReaderWithHeaders(String... lines) {
        StringBuilder builder = new StringBuilder();
        builder.append("MCTS ID,NEW MSISDN").append("\n");
        for (String line : lines) {
            builder.append(line).append("\n");
        }
        return new StringReader(builder.toString());
    }

    private Reader read(String resource) {
        return new InputStreamReader(getClass().getClassLoader().getResourceAsStream(resource));
    }


}
