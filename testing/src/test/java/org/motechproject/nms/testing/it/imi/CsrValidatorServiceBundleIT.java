package org.motechproject.nms.testing.it.imi;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.alerts.contract.AlertService;
import org.motechproject.nms.imi.exception.InvalidCsrException;
import org.motechproject.nms.imi.repository.FileAuditRecordDataService;
import org.motechproject.nms.imi.service.CdrFileService;
import org.motechproject.nms.imi.service.CsrValidatorService;
import org.motechproject.nms.imi.service.SettingsService;
import org.motechproject.nms.kilkari.repository.CallRetryDataService;
import org.motechproject.nms.kilkari.repository.SubscriberDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionPackDataService;
import org.motechproject.nms.kilkari.service.SubscriptionService;
import org.motechproject.nms.region.repository.CircleDataService;
import org.motechproject.nms.region.repository.DistrictDataService;
import org.motechproject.nms.region.repository.LanguageDataService;
import org.motechproject.nms.region.repository.LanguageLocationDataService;
import org.motechproject.nms.region.repository.StateDataService;
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
    private SettingsService settingsService;

    @Inject
    private SubscriptionService subscriptionService;

    @Inject
    private SubscriberDataService subscriberDataService;

    @Inject
    private SubscriptionPackDataService subscriptionPackDataService;

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

    @Inject
    private FileAuditRecordDataService fileAuditRecordDataService;

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
                subscriberDataService, languageDataService, languageLocationDataService, circleDataService,
                stateDataService, districtDataService);

        helper.makeRecords(1, 0, 0, 0);

        // any error inside this would throw an exception
        csrValidatorService.validateSummaryRecord(helper.getRecords().get(0));
    }


    @Test(expected = InvalidCsrException.class)
    public void testInvalid() {
        getLogger().debug("testInvalid()");

        String timestamp = DateTime.now().toString(TIME_FORMATTER);

        CsrHelper helper = new CsrHelper(timestamp, subscriptionService, subscriptionPackDataService,
                subscriberDataService, languageDataService, languageLocationDataService, circleDataService,
                stateDataService, districtDataService);

        helper.makeRecords(0, 0, 0, 1);

        csrValidatorService.validateSummaryRecord(helper.getRecords().get(0));
    }
}
