package org.motechproject.nms.kilkari.ut;

import org.junit.Before;
import org.junit.Test;
import org.motechproject.nms.kilkari.domain.Subscriber;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class SubscriberUnitTest {
    Validator validator;

    @Before
    public void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void testCallingNumberTooShort() {
        Subscriber s = new Subscriber(111111111L);

        Set<ConstraintViolation<Subscriber>> constraintViolations = validator
                .validateProperty(s, "callingNumber");

        assertEquals(1, constraintViolations.size());
        assertEquals("callingNumber must be 10 digits", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void testCallingNumberTooLong() {
        Subscriber s = new Subscriber(11111111111L);

        Set<ConstraintViolation<Subscriber>> constraintViolations = validator
                .validateProperty(s, "callingNumber");

        assertEquals(1, constraintViolations.size());
        assertEquals("callingNumber must be 10 digits", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void testCallingNumberValid() {
        Subscriber s = new Subscriber(1111111111L);

        Set<ConstraintViolation<Subscriber>> constraintViolations = validator
                .validateProperty(s, "callingNumber");

        assertEquals(0, constraintViolations.size());
    }
}
