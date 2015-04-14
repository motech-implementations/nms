package org.motechproject.nms.flw.service.impl;

import org.motechproject.nms.flw.domain.FrontLineWorker;
import org.motechproject.nms.flw.repository.FrontLineWorkerDataService;
import org.motechproject.nms.flw.service.FrontLineWorkerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Simple implementation of the {@link org.motechproject.nms.flw.service.FrontLineWorkerService} interface.
 */
@Service("frontLineWorkerService")
public class FrontLineWorkerServiceImpl implements FrontLineWorkerService {
    private FrontLineWorkerDataService frontLineWorkerDataService;

    @Autowired
    public FrontLineWorkerServiceImpl(FrontLineWorkerDataService frontLineWorkerDataService) {
        this.frontLineWorkerDataService = frontLineWorkerDataService;
    }

    @Override
    public void add(FrontLineWorker record) {
        frontLineWorkerDataService.create(record);
    }

    @Override
    public FrontLineWorker getByContactNumber(String contactNumber) {
        return frontLineWorkerDataService.findRecordByContactNumber(contactNumber);
    }

    @Override
    public List<FrontLineWorker> getRecords() {
        return frontLineWorkerDataService.retrieveAll();
    }

    @Override
    public void update(FrontLineWorker record) {
        frontLineWorkerDataService.update(record);
    }

    @Override
    public void delete(FrontLineWorker record) {
        frontLineWorkerDataService.delete(record);
    }
}
