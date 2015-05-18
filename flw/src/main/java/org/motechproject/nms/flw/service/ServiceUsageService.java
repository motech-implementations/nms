package org.motechproject.nms.flw.service;

import org.motechproject.nms.flw.domain.FrontLineWorker;
import org.motechproject.nms.flw.domain.ServiceUsage;
import org.motechproject.nms.props.domain.Service;

import java.util.List;


public interface ServiceUsageService {
    void add(ServiceUsage record);

    ServiceUsage getCurrentMonthlyUsageForFLWAndService(final FrontLineWorker frontLineWorker, final Service service);

    List<ServiceUsage> getRecords();

    void update(ServiceUsage record);

    void delete(ServiceUsage record);
}
