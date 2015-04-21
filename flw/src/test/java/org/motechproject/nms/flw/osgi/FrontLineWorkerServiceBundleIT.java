package org.motechproject.nms.flw.osgi;

import org.motechproject.nms.flw.domain.FrontLineWorker;
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
        FrontLineWorker flw = new FrontLineWorker("Test Worker", "1111111111");
        frontLineWorkerService.add(flw);

        FrontLineWorker otherFlw = frontLineWorkerDataService.findByContactNumber("1111111111");
        assertNotNull(otherFlw);

        FrontLineWorker record = frontLineWorkerService.getByContactNumber(flw.getContactNumber());
        assertEquals(flw, record);

        List<FrontLineWorker> records = frontLineWorkerService.getRecords();
        assertTrue(records.contains(flw));

        frontLineWorkerService.delete(flw);
        record = frontLineWorkerService.getByContactNumber(flw.getContactNumber());
        assertNull(record);
    }
}
