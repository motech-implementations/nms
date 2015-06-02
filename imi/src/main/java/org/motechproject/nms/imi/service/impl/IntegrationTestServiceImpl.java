package org.motechproject.nms.imi.service.impl;

import org.motechproject.nms.imi.repository.FileAuditRecordDataService;
import org.motechproject.nms.imi.service.IntegrationTestService;
import org.motechproject.server.config.SettingsFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("imiItService")
public class IntegrationTestServiceImpl implements IntegrationTestService {

    private static final String TESTING_ENVIRONMENT="testing.environment";

    @Autowired
    private FileAuditRecordDataService fileAuditRecordDataService;

    /**
     * SettingsFacade
     */
    @Autowired
    @Qualifier("imiSettings")
    private SettingsFacade settingsFacade;


    public void deleteAll() {

        if (!Boolean.parseBoolean(settingsFacade.getProperty(TESTING_ENVIRONMENT))) {
            throw new IllegalStateException("calling deleteAll() in a production environment is forbidden!");
        }

        fileAuditRecordDataService.deleteAll();
    }
}
