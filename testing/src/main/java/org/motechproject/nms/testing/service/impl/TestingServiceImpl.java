package org.motechproject.nms.testing.service.impl;

import org.motechproject.nms.kilkari.service.SubscriptionService;
import org.motechproject.nms.testing.service.TestingService;
import org.motechproject.server.config.SettingsFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("testingService")
public class TestingServiceImpl implements TestingService {


    private static final Logger LOGGER = LoggerFactory.getLogger(TestingServiceImpl.class);
    private SettingsFacade settingsFacade;
    private SubscriptionService subscriptionService;


    @Autowired
    public TestingServiceImpl(@Qualifier("testingSettings") SettingsFacade settingsFacade,
                              SubscriptionService subscriptionService) {
        this.settingsFacade = settingsFacade;
        this.subscriptionService = subscriptionService;
    }


    @Override
    public void setupDatabase() {
        LOGGER.debug("testing.foo={}", settingsFacade.getProperty("testing.foo"));
        subscriptionService.deleteAll();
    }

}

