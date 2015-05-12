package org.motechproject.nms.flw.ut;

import org.junit.Before;
import org.junit.Test;
import org.motechproject.nms.flw.domain.CallDetailRecord;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class CallDetailRecordUnitTest {
    Validator validator;

    @Before
    public void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void testCallingNumberTooShort() {
        CallDetailRecord cdr = new CallDetailRecord();
        cdr.setCallingNumber(111111111L);

        Set<ConstraintViolation<CallDetailRecord>> constraintViolations = validator
                .validateProperty(cdr, "callingNumber");

        assertEquals(1, constraintViolations.size());
        assertEquals("callingNumber must be 10 digits", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void testCallingNumberTooLong() {
        CallDetailRecord cdr = new CallDetailRecord();
        cdr.setCallingNumber(11111111111L);

        Set<ConstraintViolation<CallDetailRecord>> constraintViolations = validator
                .validateProperty(cdr, "callingNumber");

        assertEquals(1, constraintViolations.size());
        assertEquals("callingNumber must be 10 digits", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void testCallingNumberValid() {
        CallDetailRecord cdr = new CallDetailRecord();
        cdr.setCallingNumber(1111111111L);

        Set<ConstraintViolation<CallDetailRecord>> constraintViolations = validator
                .validateProperty(cdr, "callingNumber");

        assertEquals(0, constraintViolations.size());
    }
}
