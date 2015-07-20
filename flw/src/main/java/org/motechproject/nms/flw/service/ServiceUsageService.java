package org.motechproject.nms.flw.service;

import org.motechproject.nms.flw.domain.FrontLineWorker;
import org.motechproject.nms.flw.domain.ServiceUsage;
import org.motechproject.nms.props.domain.Service;


public interface ServiceUsageService {
    ServiceUsage getCurrentMonthlyUsageForFLWAndService(final FrontLineWorker frontLineWorker, final Service service);
}
