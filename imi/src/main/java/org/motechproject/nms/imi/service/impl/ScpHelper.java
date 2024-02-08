package org.motechproject.nms.imi.service.impl;


import org.motechproject.nms.imi.exception.ExecException;
import org.motechproject.server.config.SettingsFacade;


/**
 * Uses the operating system to scp & sort CDR files and scp OBD files
 */
public class ScpHelper {

    private static final String SCP_FROM_COMMAND = "imi.scp.from_command";
    private static final String SCP_FROM_COMMAND_DEFAULT = "/bin/cp {src} {dst}";
    private static final String SCP_TO_COMMAND = "imi.scp.to_command";
    private static final String SCP_TO_COMMAND_DEFAULT = "/bin/cp {src} {dst}";
    private static final String SCP_TIMEOUT_SETTING = "imi.scp.timeout";
    private static final Long SCP_TIMEOUT_DEFAULT = 60000L;

    private static final String LOCAL_OBD_DIR = "imi.local_obd_dir";
    private static final String REMOTE_OBD_DIR = "imi.remote_obd_dir";
    private static final String LOCAL_OBD_DIR_WhatsApp = "imi.local_obd_dir_whatsapp";
    private static final String REMOTE_OBD_DIR_WhatsApp = "imi.remote_obd_dir_whatsapp";
    private static final String LOCAL_CDR_DIR = "imi.local_cdr_dir";
    private static final String REMOTE_CDR_DIR = "imi.remote_cdr_dir";
    private static final String LOCAL_CDR_DIR_WhatsApp = "imi.local_cdr_dir_whatsapp";
    private static final String REMOTE_CDR_DIR_WhatsApp = "imi.remote_cdr_dir_whatsapp";

    private SettingsFacade settingsFacade;


    public ScpHelper(SettingsFacade settingsFacade) {
        this.settingsFacade = settingsFacade;
    }


    private String getSettingWithDefault(String setting, String defaultValue) {
        String s = settingsFacade.getProperty(setting);
        if (s == null || s.isEmpty()) {
            return  defaultValue;
        }
        return s;
    }


    private String scpFromCommand(String src, String dst) {
        String command = getSettingWithDefault(SCP_FROM_COMMAND, SCP_FROM_COMMAND_DEFAULT);

        return command.replace("{src}", src).replace("{dst}", dst);
    }


    private String scpToCommand(String src, String dst) {
        String command = getSettingWithDefault(SCP_TO_COMMAND, SCP_TO_COMMAND_DEFAULT);

        return command.replace("{src}", src).replace("{dst}", dst);
    }


    private Long getScpTimeout() {
        try {
            return Long.parseLong(settingsFacade.getProperty(SCP_TIMEOUT_SETTING));
        } catch (NumberFormatException e) {
            return SCP_TIMEOUT_DEFAULT;
        }
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

        String command = scpFromCommand(remoteCdrFile(file), localDir);
        ExecHelper execHelper = new ExecHelper();
        execHelper.exec(command, getScpTimeout());
    }


    public String localObdFile(String file) {
        String localFile = settingsFacade.getProperty(LOCAL_OBD_DIR);
        localFile += localFile.endsWith("/") ? "" : "/";
        localFile += file;
        return localFile;
    }

    public String localWhatsAppObdFile(String file) {
        String localFile = settingsFacade.getProperty(LOCAL_OBD_DIR_WhatsApp);
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

        String command = scpToCommand(localObdFile(file), remoteDir);
        ExecHelper execHelper = new ExecHelper();
        execHelper.exec(command, getScpTimeout());
    }

    public void scpWhatsAppObdToRemote(String file) throws ExecException {

        String remoteDir = settingsFacade.getProperty(REMOTE_OBD_DIR_WhatsApp);

        String command = scpToCommand(localWhatsAppObdFile(file), remoteDir);
        ExecHelper execHelper = new ExecHelper();
        execHelper.exec(command, getScpTimeout());
    }
}
