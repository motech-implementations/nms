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


    public void copyFrom(String remoteSource, String localDestination) throws ExecException {

        String command = String.format("%s %s%s@%s:%s %s", getScpBinary(), identityOption(getScpIdentity()),
                getScpUser(), getScpHost(), remoteSource, localDestination);
        ExecHelper execHelper = new ExecHelper();
        execHelper.exec(command, getScpTimeout());
    }


    public void copyTo(String localSource, String remoteDestination) throws ExecException {
        String command = String.format("%s %s%s %s@%s:%s", getScpBinary(), identityOption(getScpIdentity()),
                localSource, getScpUser(), getScpHost(), remoteDestination);
        ExecHelper execHelper = new ExecHelper();
        execHelper.exec(command, getScpTimeout());
    }
}
