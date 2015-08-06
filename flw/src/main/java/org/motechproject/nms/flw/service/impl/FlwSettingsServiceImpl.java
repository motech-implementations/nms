package org.motechproject.nms.flw.service.impl;

import org.motechproject.nms.flw.service.FlwSettingsService;
import org.motechproject.server.config.SettingsFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * helper service class created to facilitate ITs.
 */
@Service("flwSettingsService")
public class FlwSettingsServiceImpl implements FlwSettingsService {
    private SettingsFacade settingsFacade;

    @Autowired
    FlwSettingsServiceImpl(SettingsFacade settingsFacade) {
        this.settingsFacade = settingsFacade;
    }


    public SettingsFacade getSettingsFacade() {
        return settingsFacade;
    }
}
