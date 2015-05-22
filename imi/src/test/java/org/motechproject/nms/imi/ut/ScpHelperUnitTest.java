package org.motechproject.nms.imi.ut;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.motechproject.nms.imi.exception.ExecException;
import org.motechproject.nms.imi.service.impl.ScpHelper;
import org.motechproject.server.config.SettingsFacade;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class ScpHelperUnitTest {

    private static final String IMI_PROPERTIES = "imi.properties";
    private static final String SCP_USER = "imi.scp.user";
    private static final String SCP_HOST = "imi.scp.host";
    private static final String SCP_IDENTITY = "imi.scp.identity";

    private static final String LOCAL_OBD_DIR = "imi.local_obd_dir";
    private static final String REMOTE_OBD_DIR = "imi.remote_obd_dir";
    private static final String LOCAL_CDR_DIR = "imi.local_cdr_dir";
    private static final String REMOTE_CDR_DIR = "imi.remote_cdr_dir";

    private SettingsFacade settingsFacade;
    private String userBackup;
    private String hostBackup;
    private String identityBackup;
    private String localCdrDirBackup;
    private String remoteCdrDirBackup;
    private String localObdDirBackup;
    private String remoteObdDirBackup;


    private String setupTestDir(String property, String dir) {
        String backup = settingsFacade.getProperty(property);
        File directory = new File(System.getProperty("user.home"), dir);
        directory.mkdirs();
        settingsFacade.setProperty(property, directory.getAbsolutePath());
        return backup;
    }


    @Before
    public void setupSettings() {
        settingsFacade = new SettingsFacade();

        List<Resource> configFiles = new ArrayList<>();
        configFiles.add(new ClassPathResource(IMI_PROPERTIES));
        settingsFacade.setConfigFiles(configFiles);


        userBackup = settingsFacade.getProperty(SCP_USER);
        settingsFacade.setProperty(SCP_USER, System.getProperty("user.name"));

        hostBackup = settingsFacade.getProperty(SCP_HOST);
        settingsFacade.setProperty(SCP_HOST, "localhost");

        identityBackup = settingsFacade.getProperty(SCP_IDENTITY);
        settingsFacade.setProperty(SCP_IDENTITY, "");

        String home = System.getProperty("user.home");


        localCdrDirBackup = setupTestDir(LOCAL_CDR_DIR, "cdr-local-dir-ut");
        remoteCdrDirBackup = setupTestDir(REMOTE_CDR_DIR, "cdr-remote-dir-ut");
        localObdDirBackup = setupTestDir(LOCAL_OBD_DIR, "obd-local-dir-ut");
        remoteObdDirBackup = setupTestDir(REMOTE_OBD_DIR, "obd-remote-dir-ut");
    }


    @After
    public void restoreSettings() {
        settingsFacade.setProperty(SCP_USER, userBackup);
        settingsFacade.setProperty(SCP_HOST, hostBackup);
        settingsFacade.setProperty(SCP_IDENTITY, identityBackup);
        settingsFacade.setProperty(REMOTE_OBD_DIR, remoteObdDirBackup);
        settingsFacade.setProperty(LOCAL_OBD_DIR, localObdDirBackup);
        settingsFacade.setProperty(REMOTE_CDR_DIR, remoteCdrDirBackup);
        settingsFacade.setProperty(LOCAL_CDR_DIR, localCdrDirBackup);
    }


    private void createFile(File file) throws IOException {
        FileWriter writer = new FileWriter(file);
        writer.write("Hello, world.\n");
        writer.close();
    }


    @Test
    public void verifyScpFrom() throws IOException, ExecException {
        ScpHelper scpHelper = new ScpHelper(settingsFacade);
        File remoteFile = new File(scpHelper.remoteCdrFile("foo.txt"));
        createFile(remoteFile);
        scpHelper.scpCdrFromRemote("foo.txt");
        File localFile = new File(scpHelper.localCdrFile("foo.txt"));
        assertTrue(localFile.exists());
    }


    @Test
    public void verifyScpTo() throws IOException, ExecException {
        ScpHelper scpHelper = new ScpHelper(settingsFacade);
        File localFile = new File(scpHelper.localObdFile("foo.txt"));
        createFile(localFile);
        scpHelper.scpObdToRemote("foo.txt");
        File remoteFile = new File(scpHelper.remoteObdFile("foo.txt"));
        assertTrue(localFile.exists());
    }
}
