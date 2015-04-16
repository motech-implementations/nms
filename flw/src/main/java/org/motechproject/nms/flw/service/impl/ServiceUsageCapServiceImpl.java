package org.motechproject.nms.flw.service.impl;

import org.motechproject.mds.query.QueryExecution;
import org.motechproject.mds.util.InstanceSecurityRestriction;
import org.motechproject.nms.flw.domain.ServiceUsageCap;
import org.motechproject.nms.flw.repository.ServiceUsageCapDataService;
import org.motechproject.nms.flw.service.ServiceUsageCapService;
import org.motechproject.nms.location.domain.State;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.jdo.Query;

/**
 * Created by rob on 4/15/15.
 */
@Service("serviceUsageService")
public class ServiceUsageCapServiceImpl implements ServiceUsageCapService {
    private ServiceUsageCapDataService serviceUsageCapDataService;

    @Autowired
    public ServiceUsageCapServiceImpl(ServiceUsageCapDataService serviceUsageCapDataService) {
        this.serviceUsageCapDataService = serviceUsageCapDataService;
    }

    @Override
    public ServiceUsageCap getServiceUsageCap(final State state, final org.motechproject.nms.flw.domain.Service service) {

        // Find the national cap by looking for a record with null state
        QueryExecution<ServiceUsageCap> nationalQueryExecution = new QueryExecution<ServiceUsageCap>() {
            @Override
            public ServiceUsageCap execute(Query query, InstanceSecurityRestriction restriction) {

                query.setFilter("state == null && service == flw_service");
                query.declareParameters("org.motechproject.nms.flw.domain.Service flw_service");

                return (ServiceUsageCap) query.execute(service);
            }
        };

        ServiceUsageCap nationalServiceUsageCap = serviceUsageCapDataService.executeQuery(nationalQueryExecution);

        if (null != nationalServiceUsageCap) {
            return nationalServiceUsageCap;
        }

        if (null != state) {
            // Find a state cap by providing a state
            QueryExecution<ServiceUsageCap> stateQueryExecution = new QueryExecution<ServiceUsageCap>() {
                @Override
                public ServiceUsageCap execute(Query query, InstanceSecurityRestriction restriction) {

                    query.setFilter("state == flw_state && service == flw_service");
                    query.declareParameters("org.motechproject.nms.location.domain.State flw_state, org.motechproject.nms.flw.domain.Service flw_service");

                    return (ServiceUsageCap) query.execute(state, service);
                }
            };

            ServiceUsageCap stateServiceUsageCap = serviceUsageCapDataService.executeQuery(stateQueryExecution);

            if (null != stateServiceUsageCap) {
                return stateServiceUsageCap;
            }
        }

        // Usage is uncapped
        ServiceUsageCap serviceUsageCap = new ServiceUsageCap(null, null, -1);

        return serviceUsageCap;
    }
}
