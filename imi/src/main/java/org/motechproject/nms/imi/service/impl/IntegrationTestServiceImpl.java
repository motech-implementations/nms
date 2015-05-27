package org.motechproject.nms.imi.service.impl;

import org.motechproject.nms.imi.repository.FileAuditRecordDataService;
import org.motechproject.nms.imi.service.IntegrationTestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("itService")
public class IntegrationTestServiceImpl implements IntegrationTestService {
    @Autowired
    private FileAuditRecordDataService fileAuditRecordDataService;

    public void deleteAll() {
        fileAuditRecordDataService.deleteAll();
    }
}
