package org.motechproject.nms.region.ut;

import org.junit.Before;
import org.junit.Test;
import org.motechproject.nms.region.domain.District;
import org.motechproject.nms.region.domain.FullLocation;
import org.motechproject.nms.region.domain.HealthBlock;
import org.motechproject.nms.region.domain.HealthFacility;
import org.motechproject.nms.region.domain.HealthSubFacility;
import org.motechproject.nms.region.domain.State;
import org.motechproject.nms.region.domain.Taluka;
import org.motechproject.nms.region.domain.Village;
import org.motechproject.nms.region.domain.validation.ValidFullLocation;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.*;

import static org.junit.Assert.assertEquals;

public class FullLocationValidatorUnitTest {

    Validator validator;

    @Before
    public void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // Test No location
    @Test
    public void testNoLocation() {
        TestLocation testLocation = new TestLocation();

        Set<ConstraintViolation<TestLocation>> constraintViolations = validator.validate(testLocation);

        assertEquals(0, constraintViolations.size());
    }

    // Test only state
    @Test
    public void testOnlyState() {
        TestLocation testLocation = new TestLocation();
        testLocation.setState(new State("State", 1l));

        Set<ConstraintViolation<TestLocation>> constraintViolations = validator.validate(testLocation);

        assertEquals(1, constraintViolations.size());
        assertEquals("A location at District or below must be provided", constraintViolations.iterator().next().getMessage());
    }

    public void buildValidFullLocation(TestLocation testLocation) {
        State state = new State();
        state.setId(1L);
        District district = new District();
        district.setId(2L);
        Taluka taluka = new Taluka();
        taluka.setId(3L);
        Village village = new Village();
        village.setId(4L);
        HealthBlock healthBlock = new HealthBlock();
        healthBlock.setId(5L);
        HealthFacility healthFacility = new HealthFacility();
        healthFacility.setId(6L);
        HealthSubFacility healthSubFacility = new HealthSubFacility();
        healthSubFacility.setId(7L);

        state.getDistricts().add(district);
        district.setState(state);
        district.getTalukas().add(taluka);
        taluka.setDistrict(district);
        taluka.getVillages().add(village);
        village.setTaluka(taluka);
        //TODO HARITHA commented 2 lines m-n taluka hb
        //healthBlock.addTaluka(taluka);
        healthBlock.setDistrict(district);

        //taluka.addHealthBlock(healthBlock);

        healthBlock.getHealthFacilities().add(healthFacility);
        healthFacility.setHealthBlock(healthBlock);
        healthFacility.getHealthSubFacilities().add(healthSubFacility);
        healthSubFacility.setHealthFacility(healthFacility);

        testLocation.setState(state);
        testLocation.setDistrict(district);
        testLocation.setTaluka(taluka);
        testLocation.setVillage(village);
        testLocation.setHealthBlock(healthBlock);
        testLocation.setHealthFacility(healthFacility);
        testLocation.setHealthSubFacility(healthSubFacility);
    }

    // Valid FullLocation
    @Test
    public void testValidFullLocation() {
        TestLocation testLocation = new TestLocation();
        buildValidFullLocation(testLocation);

        Set<ConstraintViolation<TestLocation>> constraintViolations = validator.validate(testLocation);

        assertEquals(0, constraintViolations.size());
    }

    // Broken link in the chain tests:
    //  Test all but district
    @Test
    public void testBrokenChainNoDistrict() {
        TestLocation testLocation = new TestLocation();

        buildValidFullLocation(testLocation);
        testLocation.setDistrict(null);

        Set<ConstraintViolation<TestLocation>> constraintViolations = validator.validate(testLocation);

        assertEquals(1, constraintViolations.size());
        assertEquals("District must be set if taluka is provided", constraintViolations.iterator().next().getMessage());
    }

    //  Test all but taluka with village
    @Test
    public void testBrokenChainNoTalukaWithVillage() {
        TestLocation testLocation = new TestLocation();

        buildValidFullLocation(testLocation);
        testLocation.setTaluka(null);
        testLocation.setHealthBlock(null);
        testLocation.setHealthFacility(null);
        testLocation.setHealthSubFacility(null);

        Set<ConstraintViolation<TestLocation>> constraintViolations = validator.validate(testLocation);

        assertEquals(1, constraintViolations.size());
        assertEquals("Taluka must be set if village is provided", constraintViolations.iterator().next().getMessage());
    }

    //  Test all but health block
    @Test
    public void testBrokenChainNoHealthBlock() {
        TestLocation testLocation = new TestLocation();

        buildValidFullLocation(testLocation);
        testLocation.setHealthBlock(null);

        Set<ConstraintViolation<TestLocation>> constraintViolations = validator.validate(testLocation);

        assertEquals(1, constraintViolations.size());
        assertEquals("Health Block must be set if facility is provided", constraintViolations.iterator().next().getMessage());
    }

    //  Test all but health facility
    @Test
    public void testBrokenChainNoHealthFacility() {
        TestLocation testLocation = new TestLocation();

        buildValidFullLocation(testLocation);
        testLocation.setHealthFacility(null);

        Set<ConstraintViolation<TestLocation>> constraintViolations = validator.validate(testLocation);

        assertEquals(1, constraintViolations.size());
        assertEquals("Health Facility must be set if sub-facility is provided", constraintViolations.iterator().next().getMessage());
    }

    //  Test all but taluka with health block
    @Test
    public void testBrokenChainNoTalukaWithHealthBlock() {
        TestLocation testLocation = new TestLocation();

        buildValidFullLocation(testLocation);
        testLocation.setTaluka(null);
        testLocation.setVillage(null);

        Set<ConstraintViolation<TestLocation>> constraintViolations = validator.validate(testLocation);

        assertEquals(1, constraintViolations.size());
        assertEquals("Taluka must be set if block is provided", constraintViolations.iterator().next().getMessage());
    }

    // Test child not in parent
    //   Test district not in state
    @Test
    public void testDistrictNotInState() {
        TestLocation testLocation = new TestLocation();
        buildValidFullLocation(testLocation);

        testLocation.getState().setDistricts(Collections.<District>emptySet());
        testLocation.getDistrict().setState(null);

        Set<ConstraintViolation<TestLocation>> constraintViolations = validator.validate(testLocation);

        assertEquals(1, constraintViolations.size());
        assertEquals("District is not a child of the State", constraintViolations.iterator().next().getMessage());
    }

    //   Test taluka not in district
    @Test
    public void testTalukaNotInDistrict() {
        TestLocation testLocation = new TestLocation();
        buildValidFullLocation(testLocation);

        testLocation.getDistrict().setTalukas(new ArrayList<>());
        testLocation.getTaluka().setDistrict(null);

        Set<ConstraintViolation<TestLocation>> constraintViolations = validator.validate(testLocation);

        assertEquals(1, constraintViolations.size());
        assertEquals("Taluka is not a child of the District", constraintViolations.iterator().next().getMessage());
    }

    //   Test village not in taluka
    @Test
    public void testVillageNotInTaluka() {
        TestLocation testLocation = new TestLocation();
        buildValidFullLocation(testLocation);

        testLocation.getTaluka().setVillages(Collections.<Village>emptyList());
        testLocation.getVillage().setTaluka(null);

        Set<ConstraintViolation<TestLocation>> constraintViolations = validator.validate(testLocation);

        assertEquals(1, constraintViolations.size());
        assertEquals("Village is not a child of the Taluka", constraintViolations.iterator().next().getMessage());
    }

    //   Test health facility not in health block
    @Test
    public void testHealthFacilityNotInHealthBlock() {
        TestLocation testLocation = new TestLocation();
        buildValidFullLocation(testLocation);

        testLocation.getHealthBlock().setHealthFacilities(Collections.<HealthFacility>emptyList());
        testLocation.getHealthFacility().setHealthBlock(null);

        Set<ConstraintViolation<TestLocation>> constraintViolations = validator.validate(testLocation);

        assertEquals(1, constraintViolations.size());
        assertEquals("Health Facility is not a child of the Health Block", constraintViolations.iterator().next().getMessage());
    }

    //   Test health sub facility not in health facilty
    @Test
    public void testHealthSubFacilityNotInHealthFacility() {
        TestLocation testLocation = new TestLocation();
        buildValidFullLocation(testLocation);

        testLocation.getHealthFacility().setHealthSubFacilities(Collections.<HealthSubFacility>emptyList());
        testLocation.getHealthSubFacility().setHealthFacility(null);

        Set<ConstraintViolation<TestLocation>> constraintViolations = validator.validate(testLocation);

        assertEquals(1, constraintViolations.size());
        assertEquals("Health Sub-Facility is not a child of the Health Facility", constraintViolations.iterator().next().getMessage());
    }
}

@ValidFullLocation
class TestLocation implements FullLocation {
    State state;
    District district;
    Taluka taluka;
    Village village;
    HealthBlock healthBlock;
    HealthFacility healthFacility;
    HealthSubFacility healthSubFacility;

    @Override
    public State getState() {
        return state;
    }

    @Override
    public void setState(State state) {
        this.state = state;
    }

    @Override
    public District getDistrict() {
        return district;
    }

    @Override
    public void setDistrict(District district) {
        this.district = district;
    }

    @Override
    public Taluka getTaluka() {
        return taluka;
    }

    @Override
    public void setTaluka(Taluka taluka) {
        this.taluka = taluka;
    }

    @Override
    public Village getVillage() {
        return village;
    }

    @Override
    public void setVillage(Village village) {
        this.village = village;
    }

    @Override
    public HealthBlock getHealthBlock() {
        return healthBlock;
    }

    @Override
    public void setHealthBlock(HealthBlock healthBlock) {
        this.healthBlock = healthBlock;
    }

    @Override
    public HealthFacility getHealthFacility() {
        return healthFacility;
    }

    @Override
    public void setHealthFacility(HealthFacility healthFacility) {
        this.healthFacility = healthFacility;
    }

    @Override
    public HealthSubFacility getHealthSubFacility() {
        return healthSubFacility;
    }

    @Override
    public void setHealthSubFacility(HealthSubFacility healthSubFacility) {
        this.healthSubFacility = healthSubFacility;
    }
}