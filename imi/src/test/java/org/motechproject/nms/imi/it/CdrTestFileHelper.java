package org.motechproject.nms.imi.it;

import org.motechproject.nms.imi.service.SettingsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

public class CdrTestFileHelper {

    private final static String TEST_DATE = "20150506070809";
    private final static String TEST_OBD_FILENAME = String.format("OBD_%s.csv", TEST_DATE);
    private final static String TEST_CDR_DETAIL_FILENAME = String.format("cdrDetail_%s", TEST_OBD_FILENAME);
    private final static String TEST_CDR_SUMMARY_FILENAME = String.format("cdrSummary_%s", TEST_OBD_FILENAME);

    private static final Logger LOGGER = LoggerFactory.getLogger(CdrTestFileHelper.class);

    private SettingsService settingsService;


    public void init(SettingsService settingsService) {
        this.settingsService = settingsService;
    }


    private File cdrDirectory() {
        File userDir = new File(System.getProperty("user.home"));
        String cdrDirProp = settingsService.getSettingsFacade().getProperty("imi.cdr_file_directory");
        return new File(userDir, cdrDirProp);
    }


    public String obdFileName() {
        return TEST_OBD_FILENAME;
    }


    public String cdrSummaryFileName() {
        return TEST_CDR_SUMMARY_FILENAME;
    }


    public String cdrDetailFileName() {
        return TEST_CDR_DETAIL_FILENAME;
    }


    public void copyCdrSummaryFile() throws IOException {
        File dstDirectory = cdrDirectory();
        String inputFile = String.format("test-files/%s", cdrSummaryFileName());
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                getClass().getClassLoader().getResourceAsStream(inputFile)));
        File dstFile = new File(cdrDirectory(), cdrSummaryFileName());
        if (dstDirectory.mkdirs()) {
            LOGGER.info("Created required directories for {}", dstDirectory);
        }
        LOGGER.info("Copying {} to {}", inputFile, dstFile);
        BufferedWriter writer = new BufferedWriter(new FileWriter(dstFile));
        String s;
        while ((s = reader.readLine()) != null) {
            writer.write(s);
            writer.write("\n");
        }

        writer.close();
        reader.close();
    }
}
