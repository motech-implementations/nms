package org.motechproject.nms.testing.it.flw;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.nms.flw.domain.ServiceUsageCap;
import org.motechproject.nms.flw.repository.ServiceUsageCapDataService;
import org.motechproject.nms.flw.service.ServiceUsageCapService;
import org.motechproject.nms.props.domain.Service;
import org.motechproject.nms.props.repository.DeployedServiceDataService;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Verify that HelloWorldService present, functional.
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@ExamFactory(MotechNativeTestContainerFactory.class)
public class ServiceUsageCapServiceBundleIT extends BasePaxIT {
    @Inject
    ServiceUsageCapDataService serviceUsageCapDataService;

    @Inject
    ServiceUsageCapService serviceUsageCapService;

    @Inject
    StateDataService stateDataService;

    @Inject
    DeployedServiceDataService deployedServiceDataService;

    @Inject
    TestingService testingService;

    private void setupData() {
        testingService.clearDatabase();
        deployedServiceDataService.deleteAll();
        serviceUsageCapDataService.deleteAll();
        stateDataService.deleteAll();
    }

    @Test
    public void testServiceUsageServicePresent() throws Exception {
        assertNotNull(serviceUsageCapService);
    }

    @Test
    public void testUncappedService() throws Exception {
        setupData();

        State state = new State("New Jersey", 1l);
        stateDataService.create(state);

        ServiceUsageCap serviceUsageCap = serviceUsageCapService.getServiceUsageCap(state, Service.MOBILE_ACADEMY);

        assertEquals(null, serviceUsageCap.getState());
        assertEquals(null, serviceUsageCap.getService());
        assertEquals(-1, serviceUsageCap.getMaxUsageInPulses());

        stateDataService.delete(state);
    }

    @Test
    public void testCappedServiceNoNational() throws Exception {
        setupData();

        State state = new State("New Jersey", 1l);
        stateDataService.create(state);

        ServiceUsageCap serviceUsageCap = new ServiceUsageCap(state, Service.MOBILE_ACADEMY, 100);
        serviceUsageCapDataService.create(serviceUsageCap);

        serviceUsageCap = serviceUsageCapService.getServiceUsageCap(state, Service.MOBILE_ACADEMY);

        assertEquals(state, serviceUsageCap.getState());
        assertEquals(Service.MOBILE_ACADEMY, serviceUsageCap.getService());
        assertEquals(100, serviceUsageCap.getMaxUsageInPulses());

        serviceUsageCapDataService.delete(serviceUsageCap);
        stateDataService.delete(state);
    }

    @Test
    public void testNationalCappedService() throws Exception {
        setupData();

        State state = new State("New Jersey", 1l);
        stateDataService.create(state);

        ServiceUsageCap serviceUsageCap = new ServiceUsageCap(null, Service.MOBILE_ACADEMY, 100);
        serviceUsageCapDataService.create(serviceUsageCap);

        serviceUsageCap = serviceUsageCapService.getServiceUsageCap(state, Service.MOBILE_ACADEMY);

        assertEquals(null, serviceUsageCap.getState());
        assertEquals(Service.MOBILE_ACADEMY, serviceUsageCap.getService());
        assertEquals(100, serviceUsageCap.getMaxUsageInPulses());

        serviceUsageCapDataService.delete(serviceUsageCap);
        stateDataService.delete(state);
    }

    @Test
    public void testNationalAndStateCappedService() throws Exception {
        setupData();

        State state = new State("New Jersey", 1l);
        stateDataService.create(state);

        ServiceUsageCap serviceUsageCapState = new ServiceUsageCap(state, Service.MOBILE_ACADEMY, 100);
        serviceUsageCapDataService.create(serviceUsageCapState);

        ServiceUsageCap serviceUsageCapNat = new ServiceUsageCap(null, Service.MOBILE_ACADEMY, 200);
        serviceUsageCapDataService.create(serviceUsageCapNat);

        ServiceUsageCap serviceUsageCap = serviceUsageCapService.getServiceUsageCap(state, Service.MOBILE_ACADEMY);

        assertEquals(state, serviceUsageCap.getState());
        assertEquals(Service.MOBILE_ACADEMY, serviceUsageCap.getService());
        assertEquals(100, serviceUsageCap.getMaxUsageInPulses());

        serviceUsageCapDataService.delete(serviceUsageCapState);
        serviceUsageCapDataService.delete(serviceUsageCapNat);
        stateDataService.delete(state);
    }

    @Test
    public void testCappedOtherService() throws Exception {
        setupData();

        State state = new State("New Jersey", 1l);
        stateDataService.create(state);

        ServiceUsageCap serviceUsageCap1 = new ServiceUsageCap(state, Service.MOBILE_ACADEMY, 100);
        serviceUsageCapDataService.create(serviceUsageCap1);

        ServiceUsageCap serviceUsageCap = serviceUsageCapService.getServiceUsageCap(state, Service.MOBILE_KUNJI);

        assertEquals(null, serviceUsageCap.getState());
        assertEquals(null, serviceUsageCap.getService());
        assertEquals(-1, serviceUsageCap.getMaxUsageInPulses());

        serviceUsageCapDataService.delete(serviceUsageCap1);
        stateDataService.delete(state);
    }

    @Test
    public void testNullStateService() throws Exception {
        setupData();

        State state = new State("New Jersey", 1l);
        stateDataService.create(state);

        ServiceUsageCap serviceUsageCap1 = new ServiceUsageCap(state, Service.MOBILE_ACADEMY, 100);
        serviceUsageCapDataService.create(serviceUsageCap1);

        ServiceUsageCap serviceUsageCap = serviceUsageCapService.getServiceUsageCap(null, Service.MOBILE_ACADEMY);

        assertEquals(null, serviceUsageCap.getState());
        assertEquals(null, serviceUsageCap.getService());
        assertEquals(-1, serviceUsageCap.getMaxUsageInPulses());

        serviceUsageCapDataService.delete(serviceUsageCap1);
        stateDataService.delete(state);
    }

}
