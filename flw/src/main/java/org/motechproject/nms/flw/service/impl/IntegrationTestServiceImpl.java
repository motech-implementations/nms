package org.motechproject.nms.flw.service.impl;

import org.joda.time.DateTime;
import org.joda.time.Weeks;
import org.motechproject.nms.flw.domain.FrontLineWorker;
import org.motechproject.nms.flw.domain.FrontLineWorkerStatus;
import org.motechproject.nms.flw.repository.CallContentDataService;
import org.motechproject.nms.flw.repository.CallDetailRecordDataService;
import org.motechproject.nms.flw.repository.FrontLineWorkerDataService;
import org.motechproject.nms.flw.repository.ServiceUsageCapDataService;
import org.motechproject.nms.flw.repository.ServiceUsageDataService;
import org.motechproject.nms.flw.repository.WhitelistEntryDataService;
import org.motechproject.nms.flw.repository.WhitelistStateDataService;
import org.motechproject.nms.flw.service.IntegrationTestService;
import org.motechproject.server.config.SettingsFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("flwItService")
public class IntegrationTestServiceImpl implements IntegrationTestService {

    private static final String WEEKS_TO_KEEP_INVALID_FLWS = "flw.weeks_to_keep_invalid_flws";
    private static final String TESTING_ENVIRONMENT="testing.environment";

    @Autowired
    private CallContentDataService callContentDataService;
    @Autowired
    private CallDetailRecordDataService callDetailRecordDataService;
    @Autowired
    private FrontLineWorkerDataService frontLineWorkerDataService;
    @Autowired
    private ServiceUsageCapDataService serviceUsageCapDataService;
    @Autowired
    private ServiceUsageDataService serviceUsageDataService;
    @Autowired
    private WhitelistEntryDataService whitelistEntryDataService;
    @Autowired
    private WhitelistStateDataService whitelistStateDataService;
    @Autowired
    @Qualifier("flwSettings")
    private SettingsFacade settingsFacade;


    public void deleteAll() {

        if (!Boolean.parseBoolean(settingsFacade.getProperty(TESTING_ENVIRONMENT))) {
            throw new IllegalStateException("calling clearDatabase() in a production environment is forbidden!");
        }

        callContentDataService.deleteAll();
        callDetailRecordDataService.deleteAll();

        int weeks = Integer.parseInt(settingsFacade.getProperty(WEEKS_TO_KEEP_INVALID_FLWS));
        DateTime now = DateTime.now();
        for (FrontLineWorker flw: frontLineWorkerDataService.retrieveAll()) {
            if ((flw.getStatus() != FrontLineWorkerStatus.INVALID) ||
                (flw.getInvalidationDate() == null) ||
                (Math.abs(Weeks.weeksBetween(now, flw.getInvalidationDate()).getWeeks()) < weeks)) {
                flw.setStatus(FrontLineWorkerStatus.INVALID);
                flw.setInvalidationDate(DateTime.now().minusYears(1));
                frontLineWorkerDataService.update(flw);
            }
        }
        frontLineWorkerDataService.deleteAll();
        serviceUsageCapDataService.deleteAll();
        serviceUsageDataService.deleteAll();
        whitelistEntryDataService.deleteAll();
        whitelistStateDataService.deleteAll();
    }
}
