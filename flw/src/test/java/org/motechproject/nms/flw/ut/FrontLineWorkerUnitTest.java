package org.motechproject.nms.flw.ut;

import org.junit.Before;
import org.junit.Test;
import org.motechproject.nms.flw.domain.FrontLineWorker;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class FrontLineWorkerUnitTest {
    Validator validator;

    @Before
    public void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void testContactNumberTooShort() {
        FrontLineWorker flw = new FrontLineWorker(111111111L);

        Set<ConstraintViolation<FrontLineWorker>> constraintViolations = validator
                .validateProperty(flw, "contactNumber");

        assertEquals(1, constraintViolations.size());
        assertEquals("contactNumber must be 10 digits", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void testContactNumberTooLong() {
        FrontLineWorker flw = new FrontLineWorker(11111111111L);

        Set<ConstraintViolation<FrontLineWorker>> constraintViolations = validator
                .validateProperty(flw, "contactNumber");

        assertEquals(1, constraintViolations.size());
        assertEquals("contactNumber must be 10 digits", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void testContactNumberValid() {
        FrontLineWorker flw = new FrontLineWorker(1111111111L);

        Set<ConstraintViolation<FrontLineWorker>> constraintViolations = validator
                .validateProperty(flw, "contactNumber");

        assertEquals(0, constraintViolations.size());
    }
}
