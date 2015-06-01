package org.motechproject.nms.testing.service.impl;

import org.motechproject.alerts.contract.AlertsDataService;
import org.motechproject.nms.testing.service.TestingService;
import org.motechproject.server.config.SettingsFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("testingService")
public class TestingServiceImpl implements TestingService {

    private static final String TESTING_ENVIRONMENT="testing.environment";

    private static final Logger LOGGER = LoggerFactory.getLogger(TestingServiceImpl.class);

    /**
     * FLW
     */
    @Autowired
    private org.motechproject.nms.flw.service.IntegrationTestService flw;

    /**
     * IMI
     */
    @Autowired
    private org.motechproject.nms.imi.service.IntegrationTestService imi;

    /**
     * Kilkari
     */
    @Autowired
    private org.motechproject.nms.kilkari.service.IntegrationTestService kilkari;

    /**
     * Mobile Academy
     */
    @Autowired
    private org.motechproject.nms.mobileacademy.service.IntegrationTestService ma;

    /**
     * Props
     */
    @Autowired
    private org.motechproject.nms.props.service.IntegrationTestService props;

    /**
     * Region
     */
    @Autowired
    private org.motechproject.nms.region.service.IntegrationTestService region;


    /**
     * MOTECH Alerts
     */
    @Autowired
    private AlertsDataService alertsDataService;

    /**
     * SettingsFacade
     */
    @Autowired
    @Qualifier("testingSettings")
    private SettingsFacade settingsFacade;



    @Override
    public void clearDatabase() {
        LOGGER.debug("clearDatabase()");

        if (!Boolean.parseBoolean(settingsFacade.getProperty(TESTING_ENVIRONMENT))) {
            throw new IllegalStateException("calling clearDatabase() in a production environment is forbidden!");
        }

        /**
         * FLW
         */
        flw.deleteAll();

        /**
         * IMI
         */
        imi.deleteAll();

        /**
         * Kilkari
         */
        kilkari.deleteAll();

        /**
         * Mobile Academy
         */
        ma.deleteAll();

        /**
         * Props
         */
        props.deleteAll();

        /**
         * Region
         */
        region.deleteAll();

        /**
         * Alerts
         */
        alertsDataService.deleteAll();
    }

}

