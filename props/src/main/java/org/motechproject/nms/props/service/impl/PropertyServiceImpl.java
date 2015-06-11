package org.motechproject.nms.props.service.impl;

import org.motechproject.mds.query.QueryExecution;
import org.motechproject.mds.util.InstanceSecurityRestriction;
import org.motechproject.nms.props.repository.DeployedServiceDataService;
import org.motechproject.nms.props.service.PropertyService;
import org.motechproject.nms.region.domain.State;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.jdo.Query;

@Service("propertyService")
public class PropertyServiceImpl implements PropertyService {
    private DeployedServiceDataService deployedServiceDataService;

    @Autowired
    public PropertyServiceImpl(DeployedServiceDataService deployedServiceDataService) {
        this.deployedServiceDataService = deployedServiceDataService;
    }

    @Override
    public boolean isServiceDeployedInState(final org.motechproject.nms.props.domain.Service service, final State state) {
        QueryExecution<Long> stateQueryExecution = new QueryExecution<Long>() {
            @Override
            public Long execute(Query query, InstanceSecurityRestriction restriction) {

                query.setFilter("state == _state && service == _service");
                query.declareParameters("org.motechproject.nms.region.domain.State _state, org.motechproject.nms.props.domain.Service _service");
                query.setResult("count(state)");
                query.setUnique(true);

                return (Long) query.execute(state, service);
            }
        };

        Long isDeployed = deployedServiceDataService.executeQuery(stateQueryExecution);

        if (isDeployed != null && isDeployed > 0) {
            return true;
        }

        return false;
    }
}
