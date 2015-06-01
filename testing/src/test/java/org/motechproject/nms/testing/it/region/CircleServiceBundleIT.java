package org.motechproject.nms.testing.it.region;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.nms.region.domain.Circle;
import org.motechproject.nms.region.domain.District;
import org.motechproject.nms.region.domain.HealthBlock;
import org.motechproject.nms.region.domain.HealthFacility;
import org.motechproject.nms.region.domain.HealthFacilityType;
import org.motechproject.nms.region.domain.HealthSubFacility;
import org.motechproject.nms.region.domain.State;
import org.motechproject.nms.region.domain.Taluka;
import org.motechproject.nms.region.domain.Village;
import org.motechproject.nms.region.repository.CircleDataService;
import org.motechproject.nms.region.repository.DistrictDataService;
import org.motechproject.nms.region.repository.HealthBlockDataService;
import org.motechproject.nms.region.repository.HealthFacilityDataService;
import org.motechproject.nms.region.repository.HealthFacilityTypeDataService;
import org.motechproject.nms.region.repository.HealthSubFacilityDataService;
import org.motechproject.nms.region.repository.StateDataService;
import org.motechproject.nms.region.repository.TalukaDataService;
import org.motechproject.nms.region.repository.VillageDataService;
import org.motechproject.testing.osgi.BasePaxIT;
import org.motechproject.testing.osgi.container.MotechNativeTestContainerFactory;
import org.ops4j.pax.exam.ExamFactory;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@ExamFactory(MotechNativeTestContainerFactory.class)
public class CircleServiceBundleIT extends BasePaxIT {

    @Inject
    private CircleDataService circleDataService;

    @Inject
    private StateDataService stateDataService;

    @Inject
    private DistrictDataService districtDataService;

    @Inject
    private TalukaDataService talukaDataService;

    @Inject
    private VillageDataService villageDataService;

    @Inject
    private HealthBlockDataService healthBlockDataService;

    @Inject
    private HealthFacilityTypeDataService healthFacilityTypeDataService;

    @Inject
    private HealthFacilityDataService healthFacilityDataService;

    @Inject
    private HealthSubFacilityDataService healthSubFacilityDataService;

    District district;
    Taluka taluka;
    Village village;
    HealthBlock healthBlock;
    HealthFacilityType healthFacilityType;
    HealthFacility healthFacility;
    HealthSubFacility healthSubFacility;

    // Circle 1           -> State 1
    // Circle 2           -> State 2, State 3
    // Circle 3, Circle 4 -> State 4
    private void setupData() {
        healthSubFacilityDataService.deleteAll();
        healthFacilityDataService.deleteAll();
        healthFacilityTypeDataService.deleteAll();
        healthBlockDataService.deleteAll();
        villageDataService.deleteAll();
        talukaDataService.deleteAll();
        districtDataService.deleteAll();
        stateDataService.deleteAll();
        circleDataService.deleteAll();

        healthSubFacility = new HealthSubFacility();
        healthSubFacility.setName("Health Sub Facility 1");
        healthSubFacility.setRegionalName("Health Sub Facility 1");
        healthSubFacility.setCode(1L);

        healthFacilityType = new HealthFacilityType();
        healthFacilityType.setName("Health Facility Type 1");
        healthFacilityType.setCode(1L);

        healthFacility = new HealthFacility();
        healthFacility.setName("Health Facility 1");
        healthFacility.setRegionalName("Health Facility 1");
        healthFacility.setCode(1L);
        healthFacility.setHealthFacilityType(healthFacilityType);
        healthFacility.getHealthSubFacilities().add(healthSubFacility);

        healthBlock = new HealthBlock();
        healthBlock.setName("Health Block 1");
        healthBlock.setRegionalName("Health Block 1");
        healthBlock.setHq("Health Block 1 HQ");
        healthBlock.setCode(1L);
        healthBlock.getHealthFacilities().add(healthFacility);

        village = new Village();
        village.setName("Village 1");
        village.setRegionalName("Village 1");
        village.setVcode(1L);

        taluka = new Taluka();
        taluka.setName("Taluka 1");
        taluka.setRegionalName("Taluka 1");
        taluka.setIdentity(1);
        taluka.setCode("0004");
        taluka.getVillages().add(village);
        taluka.getHealthBlocks().add(healthBlock);

        district = new District();
        district.setName("District 1");
        district.setRegionalName("District 1");
        district.setCode(1L);
        district.getTalukas().add(taluka);

        State state1 = new State();
        state1.setName("State 1");
        state1.setCode(1L);
        state1.getDistricts().add(district);

        State state2 = new State();
        state2.setName("State 2");
        state2.setCode(2L);

        State state3 = new State();
        state3.setName("State 3");
        state3.setCode(3L);

        State state4 = new State();
        state4.setName("State 4");
        state4.setCode(4L);

        stateDataService.create(state1);
        stateDataService.create(state2);
        stateDataService.create(state3);
        stateDataService.create(state4);

        Circle circle1 = new Circle();
        circle1.setName("Circle 1");

        Circle circle2 = new Circle();
        circle2.setName("Circle 2");

        Circle circle3 = new Circle();
        circle3.setName("Circle 3");

        Circle circle4 = new Circle();
        circle4.setName("Circle 4");

        circleDataService.create(circle1);
        circleDataService.create(circle2);
        circleDataService.create(circle3);
        circleDataService.create(circle4);

        circle1.getStates().add(state1);
        state1.getCircles().add(circle1);
        circleDataService.update(circle1);

        circle2.getStates().add(state2);
        state2.getCircles().add(circle2);
        circleDataService.update(circle2);

        circle2.getStates().add(state3);
        state3.getCircles().add(circle2);
        circleDataService.update(circle2);

        circle3.getStates().add(state4);
        state4.getCircles().add(circle3);
        circleDataService.update(circle3);

        circle4.getStates().add(state4);
        state4.getCircles().add(circle4);
        circleDataService.update(circle4);
    }

    @Test
    public void testCircleSingleState() throws Exception {
        setupData();

        Circle circle = circleDataService.findByName("Circle 1");
        assertNotNull(circle);

        assertEquals(1, circle.getStates().size());

        State state = circle.getStates().iterator().next();
        assertEquals("State 1", state.getName());

        state = stateDataService.findByCode(1L);
        assertNotNull(state);

        assertEquals(1, state.getCircles().size());

        circle = state.getCircles().iterator().next();
        assertEquals("Circle 1", circle.getName());
    }

    @Test
    @Ignore
    public void testCircleMultipleStates() throws Exception {
        setupData();

        Circle circle = circleDataService.findByName("Circle 2");
        assertNotNull(circle);

        assertEquals(2, circle.getStates().size());

        State state = stateDataService.findByCode(2L);
        assertNotNull(state);

        assertEquals(1, state.getCircles().size());

        circle = state.getCircles().iterator().next();
        assertEquals("Circle 2", circle.getName());

        state = stateDataService.findByCode(3L);
        assertNotNull(state);

        assertEquals(1, state.getCircles().size());

        circle = state.getCircles().iterator().next();
        assertEquals("Circle 2", circle.getName());
    }

    @Test
    public void testMultipleCirclesSingleState() throws Exception {
        setupData();

        Circle circle = circleDataService.findByName("Circle 3");
        assertNotNull(circle);

        assertEquals(1, circle.getStates().size());

        State state = circle.getStates().iterator().next();
        assertEquals("State 4", state.getName());

        circle = circleDataService.findByName("Circle 4");
        assertNotNull(circle);

        assertEquals(1, circle.getStates().size());

        state = circle.getStates().iterator().next();
        assertEquals("State 4", state.getName());

        state = stateDataService.findByCode(4L);
        assertNotNull(state);

        assertEquals(2, state.getCircles().size());
    }

}
