package org.motechproject.nms.outbounddialer.it;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.nms.outbounddialer.service.CdrFileService;
import org.motechproject.nms.outbounddialer.web.contract.CdrFileNotificationRequest;
import org.motechproject.nms.outbounddialer.web.contract.FileInfo;
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
public class CdrFileServiceBundleIT extends BasePaxIT {

    @Inject
    CdrFileService cdrFileService;

    @Test
    public void testServicePresent() {
        assertTrue(cdrFileService != null);
    }

    @Test
    public void testInvalidRequest() {
        cdrFileService.processCdrFile(new CdrFileNotificationRequest(
                null,
                new FileInfo("bar", "000", 0),
                new FileInfo("baz", "111", 1)));
    }
}
