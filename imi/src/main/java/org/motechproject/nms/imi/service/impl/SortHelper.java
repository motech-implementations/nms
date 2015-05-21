package org.motechproject.nms.imi.service.impl;

import org.motechproject.nms.imi.exception.ExecException;
import org.motechproject.server.config.SettingsFacade;

public class SortHelper {

    private static final String SORT_BINARY_SETTING = "imi.sort.binary";
    private static final String SORT_BINARY_DEFAULT = "/usr/bin/sort";
    private static final String SORT_TIMEOUT_SETTING = "imi.sort.timeout";
    private static final Long SORT_TIMEOUT_DEFAULT = 60000L;

    private SettingsFacade settingsFacade;


    public SortHelper(SettingsFacade settingsFacade) {
        this.settingsFacade = settingsFacade;
    }


    private String getSettingWithDefault(String setting, String defaultValue) {
        String s = settingsFacade.getProperty(setting);
        if (s == null) {
            return  defaultValue;
        }
        return s;
    }


    private String getSortBinary() {
        return getSettingWithDefault(SORT_BINARY_SETTING, SORT_BINARY_DEFAULT);
    }


    private Long getSortTimeout() {
        try {
            return Long.parseLong(settingsFacade.getProperty(SORT_TIMEOUT_SETTING));
        } catch (NumberFormatException e) {
            return SORT_TIMEOUT_DEFAULT;
        }
    }


    /**
     * Sorts the provided source CDR file on requestId (col1, text) and attempt (col4, int) into the destination file
     */
    void sort(String source, String destination) throws ExecException {
        String command = String.format("%s -t , -k 1,1 -k 4,4n -o %s %s", getSortBinary(), destination, source);
        ExecHelper execHelper = new ExecHelper();
        execHelper.exec(command, getSortTimeout());
    }
}
