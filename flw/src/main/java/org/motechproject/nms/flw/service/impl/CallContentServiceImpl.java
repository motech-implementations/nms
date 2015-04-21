package org.motechproject.nms.flw.service.impl;

import org.motechproject.nms.flw.domain.CallContent;
import org.motechproject.nms.flw.repository.CallContentDataService;
import org.motechproject.nms.flw.service.CallContentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("callContentService")
public class CallContentServiceImpl implements CallContentService {
    private CallContentDataService callContentDataService;

    @Autowired
    public CallContentServiceImpl(CallContentDataService callContentDataService) {
        this.callContentDataService = callContentDataService;
    }

    @Override
    public void add(CallContent record) {
        callContentDataService.create(record);
    }

    @Override
    public void update(CallContent record) {
        callContentDataService.update(record);
    }

    @Override
    public void delete(CallContent record) {
        callContentDataService.delete(record);
    }
}
