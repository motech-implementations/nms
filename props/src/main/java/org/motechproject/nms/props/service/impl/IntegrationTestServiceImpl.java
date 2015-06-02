package org.motechproject.nms.props.service.impl;

import org.motechproject.nms.props.repository.DeployedServiceDataService;
import org.motechproject.nms.props.service.IntegrationTestService;
import org.motechproject.server.config.SettingsFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("propsItService")
public class IntegrationTestServiceImpl implements IntegrationTestService {

    private static final String TESTING_ENVIRONMENT="testing.environment";

    @Autowired
    private DeployedServiceDataService deployedServiceDataService;

    /**
     * SettingsFacade
     */
    @Autowired
    @Qualifier("propsSettings")
    private SettingsFacade settingsFacade;

    public void deleteAll() {

        if (!Boolean.parseBoolean(settingsFacade.getProperty(TESTING_ENVIRONMENT))) {
            throw new IllegalStateException("calling deleteAll() in a production environment is forbidden!");
        }

        deployedServiceDataService.deleteAll();
    }
}
