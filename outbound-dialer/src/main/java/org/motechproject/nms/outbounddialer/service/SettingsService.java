package org.motechproject.nms.outbounddialer.service;

import org.motechproject.server.config.SettingsFacade;

/**
 * Gives access to outbound-dialer.properties to ITs
 */
public interface SettingsService {
    SettingsFacade getSettingsFacade();
}
