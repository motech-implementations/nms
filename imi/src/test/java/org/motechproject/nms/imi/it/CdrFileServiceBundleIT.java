package org.motechproject.nms.imi.it;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.alerts.contract.AlertService;
import org.motechproject.nms.imi.exception.InvalidCdrFileException;
import org.motechproject.nms.imi.repository.FileAuditRecordDataService;
import org.motechproject.nms.imi.service.CdrFileService;
import org.motechproject.nms.imi.service.SettingsService;
import org.motechproject.nms.imi.service.contract.AggregateDetailsResults;
import org.motechproject.nms.imi.web.contract.FileInfo;
import org.motechproject.nms.kilkari.repository.CallRetryDataService;
import org.motechproject.nms.kilkari.repository.SubscriberDataService;
import org.motechproject.nms.kilkari.service.SubscriptionService;
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

import static org.junit.Assert.assertEquals;
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

    @Inject
    private FileAuditRecordDataService fileAuditRecordDataService;

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
    public void testParse() throws IOException, NoSuchAlgorithmException {
        getLogger().debug("testParse()");

        CdrHelper helper = new CdrHelper(settingsService, subscriptionService, subscriberDataService,
                languageDataService, languageLocationDataService, circleDataService, stateDataService,
                districtDataService, fileAuditRecordDataService);

        helper.makeCdrs(1,1,1,1);
        helper.makeCdrFile();
        FileInfo fileInfo = new FileInfo(helper.cdr(), helper.cdrChecksum(), helper.cdrCount());
        AggregateDetailsResults result = cdrFileService.processDetailFile(fileInfo);
        assertEquals(4, result.getRecords().size());
    }


    @Test(expected = IllegalStateException.class)
    public void testParseWithChecksumError() throws IOException, NoSuchAlgorithmException {
        getLogger().debug("testParseWithChecksumError()");

        CdrHelper helper = new CdrHelper(settingsService, subscriptionService, subscriberDataService,
                languageDataService, languageLocationDataService, circleDataService, stateDataService,
                districtDataService, fileAuditRecordDataService);

        helper.makeCdrs(1,1,1,1);
        helper.makeCdrFile();
        FileInfo fileInfo = new FileInfo(helper.cdr(), "invalid checksum", helper.cdrCount());
        AggregateDetailsResults result = cdrFileService.processDetailFile(fileInfo);
        assertEquals(0, result.getRecords().size());
        assertEquals(1, result.getErrors().size());
    }


    @Test
    public void testParseWithCsvErrors() throws IOException, NoSuchAlgorithmException {
        getLogger().debug("testParseWithCsvErrors()");

        CdrHelper helper = new CdrHelper(settingsService, subscriptionService, subscriberDataService,
                languageDataService, languageLocationDataService, circleDataService, stateDataService,
                districtDataService, fileAuditRecordDataService);

        helper.makeCdrs(1,1,1,1);
        helper.makeCdrFile(2);
        FileInfo fileInfo = new FileInfo(helper.cdr(), helper.cdrChecksum(), helper.cdrCount());
        AggregateDetailsResults result = cdrFileService.processDetailFile(fileInfo);
        assertEquals(2, result.getRecords().size());
        assertEquals(2, result.getErrors().size());
    }


    @Test
    public void testDispatchWithTooManyErrors() throws IOException, NoSuchAlgorithmException {
        getLogger().debug("testDispatchWithTooManyErrors()");

        CdrHelper helper = new CdrHelper(settingsService, subscriptionService, subscriberDataService,
                languageDataService, languageLocationDataService, circleDataService, stateDataService,
                districtDataService, fileAuditRecordDataService);

        helper.makeCdrs(200,0,0,0);
        helper.makeCdrFile(200);
        FileInfo fileInfo = new FileInfo(helper.cdr(), helper.cdrChecksum(), helper.cdrCount());
        try {
            cdrFileService.verifyDetailFile(fileInfo);
        } catch (InvalidCdrFileException e) {
            List<String> errors = e.getMessages();
            assertEquals(101, errors.size());
            assertEquals("The maximum number of allowed errors", errors.get(errors.size() - 1).substring(0, 36));
        }
    }


    @Test
    public void testProcess() throws IOException, NoSuchAlgorithmException {
        getLogger().debug("testProcess()");

        CdrHelper helper = new CdrHelper(settingsService, subscriptionService, subscriberDataService,
                languageDataService, languageLocationDataService, circleDataService, stateDataService,
                districtDataService, fileAuditRecordDataService);

        helper.makeCdrs(1,1,1,1);
        helper.makeCdrFile();
        FileInfo fileInfo = new FileInfo(helper.cdr(), helper.cdrChecksum(), helper.cdrCount());
        AggregateDetailsResults result = cdrFileService.processDetailFile(fileInfo);
        assertEquals(4, result.getRecords().size());
    }

    //todo: test how successfully we aggregate detail records into a summary record
}
