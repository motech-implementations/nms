package org.motechproject.nms.imi.service.impl;

import org.motechproject.nms.imi.exception.ExecException;
import org.motechproject.server.config.SettingsFacade;

public class SortHelper {

    private static final String SORT_BINARY_SETTING = "imi.sort.binary";
    private static final String SORT_BINARY_DEFAULT = "/usr/bin/sort";
    private static final String SORT_TIMEOUT_SETTING = "imi.sort.timeout";
    private static final Long SORT_TIMEOUT_DEFAULT = 60000L;
    private static final String SORTED_SUFFIX = ".sorted";
    private static final String LOCAL_CDR_DIR = "imi.local_cdr_dir";

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


    public String localCdrFile(String file) {
        String localFile = settingsFacade.getProperty(LOCAL_CDR_DIR);
        localFile += localFile.endsWith("/") ? "" : "/";
        localFile += file;
        return localFile;
    }


    public String localSortedCdrFile(String file) {
        String localFile = settingsFacade.getProperty(LOCAL_CDR_DIR);
        localFile += localFile.endsWith("/") ? "" : "/";
        localFile += file;
        localFile += SORTED_SUFFIX;
        return localFile;
    }


    /**
     * Sorts the provided source CDR file on requestId (col1, text) and attempt (col4, int) into the destination
     * file
     */
    void sort(String file) throws ExecException {
        String command = String.format("%s -t , -k 1,1r -k 4,4n -o %s %s", getSortBinary(),
                localSortedCdrFile(file), localCdrFile(file));
        ExecHelper execHelper = new ExecHelper();
        execHelper.exec(command, getSortTimeout());
    }
}
