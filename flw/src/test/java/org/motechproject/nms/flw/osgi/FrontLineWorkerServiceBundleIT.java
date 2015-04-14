package org.motechproject.nms.flw.osgi;

import org.joda.time.DateTime;
import org.motechproject.nms.flw.domain.FrontLineWorker;
import org.motechproject.nms.flw.domain.Service;
import org.motechproject.nms.flw.domain.ServiceUsage;
import org.motechproject.nms.flw.repository.FrontLineWorkerDataService;
import org.motechproject.nms.flw.repository.ServiceUsageDataService;
import org.motechproject.nms.flw.service.FrontLineWorkerService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.nms.flw.service.ServiceUsageService;
import org.motechproject.testing.osgi.BasePaxIT;
import org.motechproject.testing.osgi.container.MotechNativeTestContainerFactory;
import org.ops4j.pax.exam.ExamFactory;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;

import javax.inject.Inject;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Verify that HelloWorldService present, functional.
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@ExamFactory(MotechNativeTestContainerFactory.class)
public class FrontLineWorkerServiceBundleIT extends BasePaxIT {

    @Inject
    private FrontLineWorkerDataService frontLineWorkerDataService;

    @Inject
    private FrontLineWorkerService frontLineWorkerService;

    @Inject
    private ServiceUsageDataService serviceUsageDataService;

    @Inject
    private ServiceUsageService serviceUsageService;

    private void setupData() {
        serviceUsageDataService.deleteAll();
        frontLineWorkerDataService.deleteAll();
    }

    @Test
    public void testFrontLineWorkerServicePresent() throws Exception {
        assertNotNull(frontLineWorkerService);
    }

    @Test
    public void testFrontLineWorkerService() throws Exception {
        setupData();
        FrontLineWorker testRecord = new FrontLineWorker("Test Worker", "1111111111");
        frontLineWorkerService.add(testRecord);

        FrontLineWorker record = frontLineWorkerService.getByContactNumber(testRecord.getContactNumber());
        assertEquals(testRecord, record);

        List<FrontLineWorker> records = frontLineWorkerService.getRecords();
        assertTrue(records.contains(testRecord));

        frontLineWorkerService.delete(testRecord);
        record = frontLineWorkerService.getByContactNumber(testRecord.getContactNumber());
        assertNull(record);
    }
}
