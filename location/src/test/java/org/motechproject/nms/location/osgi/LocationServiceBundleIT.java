package org.motechproject.nms.location.osgi;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.nms.location.domain.District;
import org.motechproject.nms.location.domain.HealthBlock;
import org.motechproject.nms.location.domain.HealthFacility;
import org.motechproject.nms.location.domain.HealthFacilityType;
import org.motechproject.nms.location.domain.HealthSubFacility;
import org.motechproject.nms.location.domain.State;
import org.motechproject.nms.location.domain.Taluka;
import org.motechproject.nms.location.domain.Village;
import org.motechproject.nms.location.repository.DistrictDataService;
import org.motechproject.nms.location.repository.HealthBlockDataService;
import org.motechproject.nms.location.repository.HealthFacilityDataService;
import org.motechproject.nms.location.repository.HealthFacilityTypeDataService;
import org.motechproject.nms.location.repository.HealthSubFacilityDataService;
import org.motechproject.nms.location.repository.StateDataService;
import org.motechproject.nms.location.repository.TalukaDataService;
import org.motechproject.nms.location.repository.VillageDataService;
import org.motechproject.nms.location.service.LocationService;
import org.motechproject.testing.osgi.BasePaxIT;
import org.motechproject.testing.osgi.container.MotechNativeTestContainerFactory;
import org.ops4j.pax.exam.ExamFactory;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;

import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@ExamFactory(MotechNativeTestContainerFactory.class)
public class LocationServiceBundleIT extends BasePaxIT {
    @Inject
    private LocationService locationService;

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

    State state;
    District district;
    Taluka taluka;
    Village village;
    HealthBlock healthBlock;
    HealthFacilityType healthFacilityType;
    HealthFacility healthFacility;
    HealthSubFacility healthSubFacility;

    @Test
    public void testServicePresent() throws Exception {
        assertNotNull(locationService);
    }

    private void cleanAll() {
        healthSubFacilityDataService.deleteAll();
        healthFacilityDataService.deleteAll();
        healthFacilityTypeDataService.deleteAll();
        healthBlockDataService.deleteAll();
        villageDataService.deleteAll();
        talukaDataService.deleteAll();
        districtDataService.deleteAll();
        stateDataService.deleteAll();
    }

    private void initAll() {
        cleanAll();

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

        state = new State();
        state.setName("State 1");
        state.setCode(1L);
        state.getDistricts().add(district);
    }

    @Test(expected = ConstraintViolationException.class)
    public void testCreateStateNoName() throws Exception {
        initAll();
        state.setName(null);

        stateDataService.create(state);
    }

    @Test(expected = ConstraintViolationException.class)
    public void testCreateDistrictNoName() throws Exception {
        initAll();
        district.setName(null);

        districtDataService.create(district);
    }

    @Test(expected = ConstraintViolationException.class)
    public void testCreateTalukaNoName() throws Exception {
        initAll();
        taluka.setName(null);

        talukaDataService.create(taluka);
    }

    @Test(expected = ConstraintViolationException.class)
    public void testCreateVillageNoName() throws Exception {
        initAll();
        village.setName(null);

        // Village is the leaf, I have to create something connected to the object graph so I save the
        // taluka (it's parent) instead
        talukaDataService.create(taluka);
    }

    @Test(expected = ConstraintViolationException.class)
    public void testCreateVillageNoCode() throws Exception {
        initAll();
        village.setVcode(null);
        village.setSvid(null);

        // Village is the leaf, I have to create something connected to the object graph so I save the
        // taluka (it's parent) instead
        talukaDataService.create(taluka);
    }

    @Test(expected = ConstraintViolationException.class)
    public void testCreateHealthBlockNoName() throws Exception {
        initAll();
        healthBlock.setName(null);

        healthBlockDataService.create(healthBlock);
    }

    @Test(expected = ConstraintViolationException.class)
    public void testCreateHealthFacilityNoName() throws Exception {
        initAll();
        healthFacility.setName(null);

        healthFacilityDataService.create(healthFacility);
    }

    @Test(expected = ConstraintViolationException.class)
    public void testCreateHealthSubFacilityNoName() throws Exception {
        initAll();
        healthSubFacility.setName(null);

        healthSubFacilityDataService.create(healthSubFacility);
    }


    @Test
    @Ignore // TODO: Remove once https://applab.atlassian.net/browse/MOTECH-1678 is resolved
    public void testValidCreate() throws Exception {
        initAll();
        stateDataService.create(state);

        State newState = stateDataService.findByCode(1L);
        assertNotNull(newState);
        assertEquals(state, newState);

        List<District> districtList = newState.getDistricts();
        assertEquals(1, districtList.size());
        assertTrue(districtList.contains(district));

        List<Taluka> talukaList = districtList.get(0).getTalukas();
        assertEquals(1, talukaList.size());
        assertTrue(talukaList.contains(taluka));

        List<Village> villageList = talukaList.get(0).getVillages();
        assertEquals(1, villageList.size());
        assertTrue(villageList.contains(village));

        List<HealthBlock> healthBlockList = talukaList.get(0).getHealthBlocks();
        assertEquals(1, healthBlockList.size());
        assertTrue(healthBlockList.contains(healthBlock));

        List<HealthFacility> healthFacilityList = healthBlockList.get(0).getHealthFacilities();
        assertEquals(1, healthFacilityList.size());
        assertEquals(healthFacilityType, healthFacilityList.get(0).getHealthFacilityType());
        assertTrue(healthFacilityList.contains(healthFacility));

        List<HealthSubFacility> healthSubFacilityList = healthFacilityList.get(0).getHealthSubFacilities();
        assertEquals(1, healthSubFacilityList.size());
        assertTrue(healthSubFacilityList.contains(healthSubFacility));
    }
}
