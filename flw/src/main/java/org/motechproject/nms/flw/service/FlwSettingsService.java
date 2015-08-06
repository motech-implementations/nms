package org.motechproject.nms.flw.service;

import org.motechproject.server.config.SettingsFacade;

/**
 * Gives access to flw.properties to ITs
 */
public interface FlwSettingsService {
    SettingsFacade getSettingsFacade();
}
