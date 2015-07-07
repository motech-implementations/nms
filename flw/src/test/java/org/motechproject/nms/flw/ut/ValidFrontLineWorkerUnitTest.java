package org.motechproject.nms.flw.ut;

import org.junit.Before;
import org.junit.Test;
import org.motechproject.nms.flw.domain.FrontLineWorker;
import org.motechproject.nms.flw.domain.FrontLineWorkerStatus;
import org.motechproject.nms.region.domain.District;
import org.motechproject.nms.region.domain.State;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class ValidFrontLineWorkerUnitTest {

    Validator validator;
    State state;
    District district;

    @Before
    public void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        state = new State();
        state.setId(1L);
        district = new District();
        district.setId(2L);

        state.getDistricts().add(district);
        district.setState(state);
    }

    // Test Valid
    @Test
    public void testActiveValid() {
        FrontLineWorker flw = new FrontLineWorker(1111111111L);
        flw.setStatus(FrontLineWorkerStatus.ACTIVE);
        flw.setState(state);
        flw.setDistrict(district);

        Set<ConstraintViolation<FrontLineWorker>> constraintViolations = validator.validate(flw);

        assertEquals(0, constraintViolations.size());
    }

    // Test Active no state
    @Test
    public void testActiveNoStateInValid() {
        FrontLineWorker flw = new FrontLineWorker(1111111111L);
        flw.setStatus(FrontLineWorkerStatus.ACTIVE);
        flw.setDistrict(district);

        Set<ConstraintViolation<FrontLineWorker>> constraintViolations = validator.validate(flw);

        // One violation is Active without state and district the other is from @FullLocatinValidator
        assertEquals(2, constraintViolations.size());
    }

    // Test Active no district
    @Test
    public void testActiveNoDistrictInValid() {
        FrontLineWorker flw = new FrontLineWorker(1111111111L);
        flw.setStatus(FrontLineWorkerStatus.ACTIVE);
        flw.setState(state);

        Set<ConstraintViolation<FrontLineWorker>> constraintViolations = validator.validate(flw);

        // One violation is Active without state and district the other is from @FullLocatinValidator
        assertEquals(2, constraintViolations.size());
    }

    // Test Active no district and state
    @Test
    public void testActiveNoDistrictNoStateInValid() {
        FrontLineWorker flw = new FrontLineWorker(1111111111L);
        flw.setStatus(FrontLineWorkerStatus.ACTIVE);

        Set<ConstraintViolation<FrontLineWorker>> constraintViolations = validator.validate(flw);

        assertEquals(1, constraintViolations.size());
        assertEquals("Active FLWs must have State and District set.", constraintViolations.iterator().next().getMessage());
    }

    // Test ANONYMOUS no location
    @Test
    public void testANONYMOUSValid() {
        FrontLineWorker flw = new FrontLineWorker(1111111111L);
        flw.setStatus(FrontLineWorkerStatus.ANONYMOUS);

        Set<ConstraintViolation<FrontLineWorker>> constraintViolations = validator.validate(flw);

        assertEquals(0, constraintViolations.size());
    }

    // Test INACTIVE no location
    @Test
    public void testINACTIVEValid() {
        FrontLineWorker flw = new FrontLineWorker(1111111111L);
        flw.setStatus(FrontLineWorkerStatus.INACTIVE);

        Set<ConstraintViolation<FrontLineWorker>> constraintViolations = validator.validate(flw);

        assertEquals(0, constraintViolations.size());
    }

    // Test INVALID no location
    @Test
    public void testINVALIDValid() {
        FrontLineWorker flw = new FrontLineWorker(1111111111L);
        flw.setStatus(FrontLineWorkerStatus.INVALID);

        Set<ConstraintViolation<FrontLineWorker>> constraintViolations = validator.validate(flw);

        assertEquals(0, constraintViolations.size());
    }
}
