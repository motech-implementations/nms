package org.motechproject.nms.flw.service.impl;

import org.motechproject.nms.flw.repository.CallContentDataService;
import org.motechproject.nms.flw.repository.CallDetailRecordDataService;
import org.motechproject.nms.flw.repository.FrontLineWorkerDataService;
import org.motechproject.nms.flw.repository.ServiceUsageCapDataService;
import org.motechproject.nms.flw.repository.ServiceUsageDataService;
import org.motechproject.nms.flw.repository.WhitelistEntryDataService;
import org.motechproject.nms.flw.repository.WhitelistStateDataService;
import org.motechproject.nms.flw.service.IntegrationTestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("itService")
public class IntegrationTestServiceImpl implements IntegrationTestService {

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

    public void deleteAll() {
        callContentDataService.deleteAll();
        callDetailRecordDataService.deleteAll();
        frontLineWorkerDataService.deleteAll();
        serviceUsageCapDataService.deleteAll();
        serviceUsageDataService.deleteAll();
        whitelistEntryDataService.deleteAll();
        whitelistStateDataService.deleteAll();
    }
}
