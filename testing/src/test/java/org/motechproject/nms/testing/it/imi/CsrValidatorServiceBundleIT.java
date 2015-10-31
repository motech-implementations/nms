package org.motechproject.nms.testing.it.imi;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.nms.imi.exception.InvalidCsrException;
import org.motechproject.nms.imi.service.CsrValidatorService;
import org.motechproject.nms.kilkari.dto.CallSummaryRecordDto;
import org.motechproject.nms.kilkari.repository.SubscriberDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionPackDataService;
import org.motechproject.nms.kilkari.service.SubscriptionService;
import org.motechproject.nms.region.repository.CircleDataService;
import org.motechproject.nms.region.repository.DistrictDataService;
import org.motechproject.nms.region.repository.LanguageDataService;
import org.motechproject.nms.region.repository.StateDataService;
import org.motechproject.nms.region.service.DistrictService;
import org.motechproject.nms.region.service.LanguageService;
import org.motechproject.nms.testing.it.utils.CsrHelper;
import org.motechproject.nms.testing.service.TestingService;
import org.motechproject.testing.osgi.BasePaxIT;
import org.motechproject.testing.osgi.container.MotechNativeTestContainerFactory;
import org.ops4j.pax.exam.ExamFactory;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;

import javax.inject.Inject;

import static org.junit.Assert.assertTrue;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@ExamFactory(MotechNativeTestContainerFactory.class)
public class CsrValidatorServiceBundleIT extends BasePaxIT {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormat.forPattern("yyyyMMddHHmmss");

    @Inject
    CsrValidatorService csrValidatorService;
    @Inject
    SubscriptionService subscriptionService;
    @Inject
    SubscriberDataService subscriberDataService;
    @Inject
    SubscriptionPackDataService subscriptionPackDataService;
    @Inject
    LanguageDataService languageDataService;
    @Inject
    LanguageService languageService;
    @Inject
    CircleDataService circleDataService;
    @Inject
    StateDataService stateDataService;
    @Inject
    DistrictDataService districtDataService;
    @Inject
    DistrictService districtService;
    @Inject
    TestingService testingService;


    @Before
    public void cleanupDatabase() {
        testingService.clearDatabase();
    }


    @Test
    public void testServicePresent() {
        getLogger().debug("testServicePresent()");
        assertTrue(csrValidatorService != null);
    }


    @Test
    public void testValid() {
        getLogger().debug("testValid()");

        String timestamp = DateTime.now().toString(TIME_FORMATTER);

        CsrHelper helper = new CsrHelper(timestamp, subscriptionService, subscriptionPackDataService,
                subscriberDataService, languageDataService, languageService, circleDataService, stateDataService,
                districtDataService, districtService);

        helper.makeRecords(1, 0, 0, 0);

        // any error inside this would throw an exception
        csrValidatorService.validateSummaryRecord(helper.getRecords().get(0));
    }


    @Test
    public void testInvalidSubscription() {
        getLogger().debug("testInvalidSubscription()");

        String timestamp = DateTime.now().toString(TIME_FORMATTER);

        CsrHelper helper = new CsrHelper(timestamp, subscriptionService, subscriptionPackDataService,
                subscriberDataService, languageDataService, languageService, circleDataService, stateDataService,
                districtDataService, districtService);

        // Invalid CSR - invalid subscription
        helper.makeRecords(0, 0, 0, 1);

        // An invalid subscription should not throw
        csrValidatorService.validateSummaryRecord(helper.getRecords().get(0));
    }


    @Test(expected = InvalidCsrException.class)
    public void testInvalid() {
        getLogger().debug("testInvalid()");

        String timestamp = DateTime.now().toString(TIME_FORMATTER);

        CsrHelper helper = new CsrHelper(timestamp, subscriptionService, subscriptionPackDataService,
                subscriberDataService, languageDataService, languageService, circleDataService, stateDataService,
                districtDataService, districtService);

        helper.makeRecords(1, 0, 0, 0);

        CallSummaryRecordDto csr = helper.getRecords().get(0);
        csr.setCircle("INVALID_CIRCLE");

        // An invalid circle should throw
        csrValidatorService.validateSummaryRecord(csr);
    }
}
