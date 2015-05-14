package org.motechproject.nms.props.ut;

import org.junit.Before;
import org.junit.Test;
import org.motechproject.nms.kilkari.domain.CallRetry;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class CallRetryUnitTest {
    Validator validator;

    @Before
    public void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void testMsisdnTooShort() {
        CallRetry cr = new CallRetry();
        cr.setMsisdn(111111111L);

        Set<ConstraintViolation<CallRetry>> constraintViolations = validator
                .validateProperty(cr, "msisdn");

        assertEquals(1, constraintViolations.size());
        assertEquals("msisdn must be 10 digits", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void testMsisdnTooLong() {
        CallRetry cr = new CallRetry();
        cr.setMsisdn(11111111111L);

        Set<ConstraintViolation<CallRetry>> constraintViolations = validator
                .validateProperty(cr, "msisdn");

        assertEquals(1, constraintViolations.size());
        assertEquals("msisdn must be 10 digits", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void testMsisdnValid() {
        CallRetry cr = new CallRetry();
        cr.setMsisdn(1111111111L);

        Set<ConstraintViolation<CallRetry>> constraintViolations = validator
                .validateProperty(cr, "msisdn");

        assertEquals(0, constraintViolations.size());
    }
}