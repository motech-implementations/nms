package org.motechproject.nms.flw.service.impl;

import org.joda.time.DateTime;
import org.motechproject.mds.query.QueryExecution;
import org.motechproject.mds.util.InstanceSecurityRestriction;
import org.motechproject.nms.flw.domain.FrontLineWorker;
import org.motechproject.nms.flw.domain.ServiceUsage;
import org.motechproject.nms.flw.repository.ServiceUsageDataService;
import org.motechproject.nms.flw.service.ServiceUsageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.jdo.Query;
import java.util.List;

/**
 * Simple implementation of the {@link org.motechproject.nms.flw.service.FrontLineWorkerService} interface.
 */
@Service("serviceUsageService")
public class ServiceUsageServiceImpl implements ServiceUsageService {
    private ServiceUsageDataService serviceUsageDataService;

    @Autowired
    public ServiceUsageServiceImpl(ServiceUsageDataService serviceUsageDataService) {
        this.serviceUsageDataService = serviceUsageDataService;
    }

    @Override
    public void add(ServiceUsage record) {
        serviceUsageDataService.create(record);
    }

    @Override
    public ServiceUsage getCurrentMonthlyUsageForFLWAndService(final FrontLineWorker frontLineWorker, final org.motechproject.nms.flw.domain.Service service) {
        ServiceUsage serviceUsage = new ServiceUsage(frontLineWorker, service, 0, 0, 0, DateTime.now());

        QueryExecution<List<ServiceUsage>> queryExecution = new QueryExecution<List<ServiceUsage>>() {
            @Override
            public List<ServiceUsage> execute(Query query, InstanceSecurityRestriction restriction) {
                DateTime monthStart = DateTime.now().withDayOfMonth(1).withTimeAtStartOfDay();

                query.setFilter("frontLineWorker == flw && service == flw_service && timestamp >= monthStart");
                query.declareParameters("org.motechproject.nms.flw.domain.FrontLineWorker flw, org.joda.time.DateTime monthStart, org.motechproject.nms.flw.domain.Service flw_service");

                return (List<ServiceUsage>) query.execute(frontLineWorker, monthStart, service);
            }
        };

        List<ServiceUsage> serviceUsageRecords = serviceUsageDataService.executeQuery(queryExecution);

        // TODO: I'm not sure I like combining the individual service usage records into an aggregate record and using the same domain object for it.
        for (ServiceUsage serviceUsageRecord : serviceUsageRecords) {
            // Add up pulse usage, endOfUsagePromptCounter and or together welcomePrompt
            serviceUsage.setEndOfUsage(serviceUsage.getEndOfUsage() + serviceUsageRecord.getEndOfUsage());
            serviceUsage.setUsageInPulses(serviceUsage.getUsageInPulses() + serviceUsageRecord.getUsageInPulses());
            serviceUsage.setWelcomePrompt(serviceUsage.getWelcomePrompt() + serviceUsageRecord.getWelcomePrompt());
        }

        return serviceUsage;
    }

    @Override
    public List<ServiceUsage> getRecords() {
        return serviceUsageDataService.retrieveAll();
    }

    @Override
    public void update(ServiceUsage record) {
        serviceUsageDataService.update(record);
    }

    @Override
    public void delete(ServiceUsage record) {
        serviceUsageDataService.delete(record);
    }
}
