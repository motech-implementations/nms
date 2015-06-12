package org.motechproject.nms.testing.it.imi;

import org.motechproject.nms.imi.service.SettingsService;

import java.io.File;

/**
 * Common setup and helper methods used by IMI ITs
 */
public final class ImiTestHelper {

    protected static final String ADMIN_USERNAME = "motech";
    protected static final String ADMIN_PASSWORD = "motech";

    protected static final String LOCAL_OBD_DIR = "imi.local_obd_dir";
    protected static final String REMOTE_OBD_DIR = "imi.remote_obd_dir";
    protected static final String LOCAL_CDR_DIR = "imi.local_cdr_dir";
    protected static final String REMOTE_CDR_DIR = "imi.remote_cdr_dir";

    public static String setupTestDir(SettingsService settingsService, String property, String dir) {
        String backup = settingsService.getSettingsFacade().getProperty(property);
        dir = ".motech/" + dir;
        File directory = new File(System.getProperty("user.home"), dir);
        directory.mkdirs();
        settingsService.getSettingsFacade().setProperty(property, directory.getAbsolutePath());
        return backup;
    }
}
