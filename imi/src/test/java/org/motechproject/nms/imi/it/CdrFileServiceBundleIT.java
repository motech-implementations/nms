package org.motechproject.nms.imi.it;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.nms.imi.service.CdrFileService;
import org.motechproject.nms.imi.service.SettingsService;
import org.motechproject.nms.imi.web.contract.CdrFileNotificationRequest;
import org.motechproject.nms.imi.web.contract.FileInfo;
import org.motechproject.testing.osgi.BasePaxIT;
import org.motechproject.testing.osgi.container.MotechNativeTestContainerFactory;
import org.ops4j.pax.exam.ExamFactory;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;

import javax.inject.Inject;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import static org.junit.Assert.assertTrue;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@ExamFactory(MotechNativeTestContainerFactory.class)
public class CdrFileServiceBundleIT extends BasePaxIT {

    private CdrTestFileHelper helper = new CdrTestFileHelper();

    @Inject
    CdrFileService cdrFileService;

    @Inject
    SettingsService settingsService;

    @Before
    public void initFileHelper() {
        helper.init(settingsService);
    }

    @Test
    public void testServicePresent() {
        assertTrue(cdrFileService != null);
    }


    @Test
    public void testValidRequest() throws IOException, NoSuchAlgorithmException {
        helper.copyCdrSummaryFile();

        cdrFileService.processCdrFile(new CdrFileNotificationRequest(
            helper.obdFileName(),
            new FileInfo(helper.cdrSummaryFileName(), helper.summaryFileChecksum(), 0),
            new FileInfo(helper.cdrDetailFileName(), helper.detailFileChecksum(), 1)));
    }
}
