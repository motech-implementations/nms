package org.motechproject.nms.kilkari.ut;

import org.junit.Before;
import org.junit.Test;
import org.motechproject.nms.kilkari.domain.CallSummaryRecord;
import org.motechproject.nms.props.domain.StatusCode;
import org.motechproject.nms.props.domain.FinalCallStatus;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CallSummaryRecordUnitTest {
    Validator validator;

    @Before
    public void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void testMsisdnTooShort() {
        CallSummaryRecord cdr = new CallSummaryRecord();
        cdr.setMsisdn(111111111L);

        Set<ConstraintViolation<CallSummaryRecord>> constraintViolations = validator
                .validateProperty(cdr, "msisdn");

        assertEquals(1, constraintViolations.size());
        assertEquals("msisdn must be 10 digits", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void testMsisdnTooLong() {
        CallSummaryRecord cdr = new CallSummaryRecord();
        cdr.setMsisdn(11111111111L);

        Set<ConstraintViolation<CallSummaryRecord>> constraintViolations = validator
                .validateProperty(cdr, "msisdn");

        assertEquals(1, constraintViolations.size());
        assertEquals("msisdn must be 10 digits", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void testMsisdnValid() {
        CallSummaryRecord cdr = new CallSummaryRecord();
        cdr.setMsisdn(1111111111L);

        Set<ConstraintViolation<CallSummaryRecord>> constraintViolations = validator
                .validateProperty(cdr, "msisdn");

        assertEquals(0, constraintViolations.size());
    }
}
