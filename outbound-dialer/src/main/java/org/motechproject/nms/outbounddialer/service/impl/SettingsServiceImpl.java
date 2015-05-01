package org.motechproject.nms.outbounddialer.service.impl;

import org.motechproject.nms.outbounddialer.service.SettingsService;
import org.motechproject.server.config.SettingsFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("settingsService")
public class SettingsServiceImpl implements SettingsService {
    private SettingsFacade settingsFacade;

    @Autowired
    SettingsServiceImpl(SettingsFacade settingsFacade) {
        this.settingsFacade = settingsFacade;
    }


    public SettingsFacade getSettingsFacade() {
        return settingsFacade;
    }
}
