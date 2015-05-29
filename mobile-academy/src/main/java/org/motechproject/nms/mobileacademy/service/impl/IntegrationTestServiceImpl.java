package org.motechproject.nms.mobileacademy.service.impl;

import org.motechproject.nms.mobileacademy.repository.CompletionRecordDataService;
import org.motechproject.nms.mobileacademy.service.IntegrationTestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("itService")
public class IntegrationTestServiceImpl implements IntegrationTestService {

    @Autowired
    private CompletionRecordDataService completionRecordDataService;


    public void deleteAll() {
        completionRecordDataService.deleteAll();
    }
}
