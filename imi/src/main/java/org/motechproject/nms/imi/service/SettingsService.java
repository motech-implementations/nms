package org.motechproject.nms.imi.service;

import org.motechproject.server.config.SettingsFacade;

/**
 * Gives access to imi.properties to ITs
 */
public interface SettingsService {
    SettingsFacade getSettingsFacade();
}
