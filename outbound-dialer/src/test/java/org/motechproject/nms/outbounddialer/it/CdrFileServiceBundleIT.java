package org.motechproject.nms.outbounddialer.it;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.nms.outbounddialer.service.CdrFileService;
import org.motechproject.nms.outbounddialer.service.SettingsService;
import org.motechproject.nms.outbounddialer.web.contract.CdrFileNotificationRequest;
import org.motechproject.nms.outbounddialer.web.contract.FileInfo;
import org.motechproject.testing.osgi.BasePaxIT;
import org.motechproject.testing.osgi.container.MotechNativeTestContainerFactory;
import org.ops4j.pax.exam.ExamFactory;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import static org.junit.Assert.assertTrue;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@ExamFactory(MotechNativeTestContainerFactory.class)
public class CdrFileServiceBundleIT extends BasePaxIT {

    @Inject
    CdrFileService cdrFileService;

    @Inject
    SettingsService settingsService;

    @Test
    public void testServicePresent() {
        assertTrue(cdrFileService != null);
    }


    private File cdrDirectory() {
        File userDir = new File(System.getProperty("user.home"));
        String cdrDirProp = settingsService.getSettingsFacade().getProperty("outbound-dialer.cdr_file_directory");
        return new File(userDir, cdrDirProp);
    }


    private void copyTestFile(String fileName, File dstDirectory) throws IOException {
        String inputFile = String.format("test-files/%s", fileName);
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                getClass().getClassLoader().getResourceAsStream(inputFile)));
        File dstFile = new File(dstDirectory, fileName);
        getLogger().info("Copying {} to {}", inputFile, dstFile);
        BufferedWriter writer = new BufferedWriter(new FileWriter(dstFile));
        String s;
        while ((s = reader.readLine()) != null) {
            writer.write(s);
            writer.write("\n");
        }

        writer.close();
        reader.close();
    }


    @Test
    public void testValidRequest() throws IOException {
        copyTestFile("cdrDetail_OBD_20150506070809.csv", cdrDirectory());
        copyTestFile("cdrSummary_OBD_20150506070809.csv", cdrDirectory());

        cdrFileService.processCdrFile(new CdrFileNotificationRequest(
                "OBD_20150506070809.csv",
                new FileInfo("cdrSummary_OBD_20150506070809.csv", "000", 0),
                new FileInfo("cdrDetail_OBD_20150506070809.csv", "111", 1)));
    }
}
