package org.motechproject.nms.flw.service;

import org.motechproject.nms.flw.domain.FrontLineWorker;

import java.util.List;

/**
 * Simple example of a service interface.
 */
public interface FrontLineWorkerService {

    void add(FrontLineWorker frontLineWorker);

    FrontLineWorker getByContactNumber(Long contactNumber);

    List<FrontLineWorker> getRecords();

    void update(FrontLineWorker record);

    void delete(FrontLineWorker record);
}
