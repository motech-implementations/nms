package org.motechproject.nms.testing.it.props;

import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.motechproject.nms.props.domain.DeployedService;
import org.motechproject.nms.props.domain.Service;
import org.motechproject.nms.props.repository.DeployedServiceDataService;
import org.motechproject.nms.props.service.PropertyService;
import org.motechproject.nms.region.domain.State;
import org.motechproject.nms.region.repository.StateDataService;
import org.motechproject.nms.testing.service.TestingService;
import org.motechproject.testing.osgi.BasePaxIT;
import org.motechproject.testing.osgi.container.MotechNativeTestContainerFactory;
import org.ops4j.pax.exam.ExamFactory;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;

import javax.inject.Inject;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@ExamFactory(MotechNativeTestContainerFactory.class)
public class PropertyServiceBundleIT extends BasePaxIT {

    @Inject
    private StateDataService stateDataService;

    @Inject
    private DeployedServiceDataService deployedServiceDataService;

    @Inject
    private PropertyService propertyService;

    @Inject
    private TestingService testingService;

    private void cleanUp() {
        testingService.clearDatabase();
    }

    private State makeState(Long code, String name) {
        State state = stateDataService.findByCode(code);
        if (state != null) {
            return state;
        }

        state = new State();
        state.setName(name);
        state.setCode(code);

        return stateDataService.create(state);
    }

    // Test a state/service that is deployed
    @Ignore //TEMP
    public void testDeployedStateAndDeployedService() {
        cleanUp();

        State state = makeState(1L, "State 1");
        deployedServiceDataService.create(new DeployedService(state, Service.MOBILE_ACADEMY));

        assertTrue(propertyService.isServiceDeployedInState(Service.MOBILE_ACADEMY, state));
    }

    // Test a state that has a deployed service but not the one being queried
    @Ignore //TEMP
    public void testDeployedStateNotDeployedService() {
        cleanUp();

        State state = makeState(1L, "State 1");
        deployedServiceDataService.create(new DeployedService(state, Service.MOBILE_ACADEMY));

        assertFalse(propertyService.isServiceDeployedInState(Service.KILKARI, state));
    }

    // Test a state with no deployments but a service that is in a different state
    @Ignore //TEMP
    public void testNotDeployedStateDeployedService() {
        cleanUp();

        State deployed = makeState(1L, "State 1");
        deployedServiceDataService.create(new DeployedService(deployed, Service.MOBILE_ACADEMY));

        State notDeployed = makeState(2L, "State 2");

        assertFalse(propertyService.isServiceDeployedInState(Service.MOBILE_ACADEMY, notDeployed));
    }

    // Test with neither state or service deployed
    @Ignore //TEMP
    public void testNotDeployedStateNotDeployedService() {
        cleanUp();

        State notDeployed = makeState(2L, "State 2");

        assertFalse(propertyService.isServiceDeployedInState(Service.MOBILE_ACADEMY, notDeployed));
    }
}
