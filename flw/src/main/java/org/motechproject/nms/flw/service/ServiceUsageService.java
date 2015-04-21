package org.motechproject.nms.flw.service;

import org.motechproject.nms.flw.domain.FrontLineWorker;
import org.motechproject.nms.flw.domain.Service;
import org.motechproject.nms.flw.domain.ServiceUsage;

import java.util.List;


/**
 * Created by rob on 4/13/15.
 */
public interface ServiceUsageService {
    void add(ServiceUsage record);

    ServiceUsage getCurrentMonthlyUsageForFLWAndService(final FrontLineWorker frontLineWorker, final Service service);

    List<ServiceUsage> getRecords();

    void update(ServiceUsage record);

    void delete(ServiceUsage record);
}
