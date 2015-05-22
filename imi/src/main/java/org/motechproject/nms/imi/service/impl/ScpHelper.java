package org.motechproject.nms.imi.service.impl;


import org.motechproject.nms.imi.exception.ExecException;
import org.motechproject.server.config.SettingsFacade;


/**
 * Uses the operating system to scp & sort CDR files and scp OBD files
 */
public class ScpHelper {

    private static final String SCP_BINARY_SETTING = "imi.scp.binary";
    private static final String SCP_BINARY_DEFAULT = "/usr/bin/scp";
    private static final String SCP_TIMEOUT_SETTING = "imi.scp.timeout";
    private static final Long SCP_TIMEOUT_DEFAULT = 60000L;
    private static final String SCP_USER = "imi.scp.user";
    private static final String SCP_HOST = "imi.scp.host";
    private static final String SCP_IDENTITY = "imi.scp.identity";

    private static final String LOCAL_OBD_DIR = "imi.local_obd_dir";
    private static final String REMOTE_OBD_DIR = "imi.remote_obd_dir";
    private static final String LOCAL_CDR_DIR = "imi.local_cdr_dir";
    private static final String REMOTE_CDR_DIR = "imi.remote_cdr_dir";

    private SettingsFacade settingsFacade;


    public ScpHelper(SettingsFacade settingsFacade) {
        this.settingsFacade = settingsFacade;
    }


    private String getSettingWithDefault(String setting, String defaultValue) {
        String s = settingsFacade.getProperty(setting);
        if (s == null) {
            return  defaultValue;
        }
        return s;
    }


    private String getScpBinary() {
        return getSettingWithDefault(SCP_BINARY_SETTING, SCP_BINARY_DEFAULT);
    }


    private Long getScpTimeout() {
        try {
            return Long.parseLong(settingsFacade.getProperty(SCP_TIMEOUT_SETTING));
        } catch (NumberFormatException e) {
            return SCP_TIMEOUT_DEFAULT;
        }
    }


    private String getScpHost() {
        return settingsFacade.getProperty(SCP_HOST);
    }


    private String getScpUser() {
        return settingsFacade.getProperty(SCP_USER);
    }


    private String getScpIdentity() {
        return settingsFacade.getProperty(SCP_IDENTITY);
    }


    private String identityOption(String identityFile) {
        if (identityFile == null || identityFile.isEmpty()) {
            return "";
        }
        return String.format("-i %s ", identityFile);
    }


    public String remoteCdrFile(String file) {
        String remoteFile = settingsFacade.getProperty(REMOTE_CDR_DIR);
        remoteFile += remoteFile.endsWith("/") ? "" : "/";
        remoteFile += file;
        return remoteFile;
    }


    public String localCdrFile(String file) {
        String localFile = settingsFacade.getProperty(LOCAL_CDR_DIR);
        localFile += localFile.endsWith("/") ? "" : "/";
        localFile += file;
        return localFile;
    }


    public void scpCdrFromRemote(String file) throws ExecException {

        String localDir = settingsFacade.getProperty(LOCAL_CDR_DIR);

        String command = String.format("%s %s%s@%s:%s %s", getScpBinary(), identityOption(getScpIdentity()),
                getScpUser(), getScpHost(), remoteCdrFile(file), localDir);
        ExecHelper execHelper = new ExecHelper();
        execHelper.exec(command, getScpTimeout());
    }


    public String localObdFile(String file) {
        String localFile = settingsFacade.getProperty(LOCAL_OBD_DIR);
        localFile += localFile.endsWith("/") ? "" : "/";
        localFile += file;
        return localFile;
    }


    public String remoteObdFile(String file) {
        String remoteFile = settingsFacade.getProperty(REMOTE_OBD_DIR);
        remoteFile += remoteFile.endsWith("/") ? "" : "/";
        remoteFile += file;
        return remoteFile;
    }


    public void scpObdToRemote(String file) throws ExecException {

        String remoteDir = settingsFacade.getProperty(REMOTE_OBD_DIR);

        String command = String.format("%s %s%s %s@%s:%s", getScpBinary(), identityOption(getScpIdentity()),
                localObdFile(file), getScpUser(), getScpHost(), remoteDir);
        ExecHelper execHelper = new ExecHelper();
        execHelper.exec(command, getScpTimeout());
    }
}
