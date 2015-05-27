package org.motechproject.nms.props.service.impl;

import org.motechproject.nms.props.repository.DeployedServiceDataService;
import org.motechproject.nms.props.service.IntegrationTestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("itService")
public class IntegrationTestServiceImpl implements IntegrationTestService {
    @Autowired
    private DeployedServiceDataService deployedServiceDataService;

    public void deleteAll() {
        deployedServiceDataService.deleteAll();
    }
}
