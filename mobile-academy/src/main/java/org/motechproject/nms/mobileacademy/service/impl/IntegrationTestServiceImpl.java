package org.motechproject.nms.mobileacademy.service.impl;

import org.motechproject.nms.mobileacademy.repository.CompletionRecordDataService;
import org.motechproject.nms.mobileacademy.service.IntegrationTestService;
import org.motechproject.server.config.SettingsFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("maItService")
public class IntegrationTestServiceImpl implements IntegrationTestService {

    private static final String TESTING_ENVIRONMENT="testing.environment";

    @Autowired
    private CompletionRecordDataService completionRecordDataService;

    /**
     * SettingsFacade
     */
    @Autowired
    @Qualifier("maSettings")
    private SettingsFacade settingsFacade;


    public void deleteAll() {

        if (!Boolean.parseBoolean(settingsFacade.getProperty(TESTING_ENVIRONMENT))) {
            throw new IllegalStateException("calling clearDatabase() in a production environment is forbidden!");
        }

        completionRecordDataService.deleteAll();
    }
}
