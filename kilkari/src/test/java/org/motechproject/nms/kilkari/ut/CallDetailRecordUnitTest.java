package org.motechproject.nms.kilkari.ut;

import org.junit.Before;
import org.junit.Test;
import org.motechproject.nms.kilkari.domain.CallDetailRecord;
import org.motechproject.nms.kilkari.domain.StatusCode;
import org.motechproject.nms.props.domain.CallStatus;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CallDetailRecordUnitTest {
    Validator validator;

    @Before
    public void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void testMsisdnTooShort() {
        CallDetailRecord cdr = new CallDetailRecord();
        cdr.setMsisdn(111111111L);

        Set<ConstraintViolation<CallDetailRecord>> constraintViolations = validator
                .validateProperty(cdr, "msisdn");

        assertEquals(1, constraintViolations.size());
        assertEquals("msisdn must be 10 digits", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void testMsisdnTooLong() {
        CallDetailRecord cdr = new CallDetailRecord();
        cdr.setMsisdn(11111111111L);

        Set<ConstraintViolation<CallDetailRecord>> constraintViolations = validator
                .validateProperty(cdr, "msisdn");

        assertEquals(1, constraintViolations.size());
        assertEquals("msisdn must be 10 digits", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void testMsisdnValid() {
        CallDetailRecord cdr = new CallDetailRecord();
        cdr.setMsisdn(1111111111L);

        Set<ConstraintViolation<CallDetailRecord>> constraintViolations = validator
                .validateProperty(cdr, "msisdn");

        assertEquals(0, constraintViolations.size());
    }

    @Test(expected=IllegalStateException.class)
    public void testTooFewFields() {
        CallDetailRecord cdr = CallDetailRecord.fromCsvLine("a,b");
        assertNotNull(cdr);
    }

    @Test(expected=IllegalStateException.class)
    public void testTooManyFields() {
        CallDetailRecord cdr = CallDetailRecord.fromCsvLine("a,b,1,d,e,f,g,h,i,j,k,l,m,o");
        assertNotNull(cdr);
    }

    @Test(expected=NumberFormatException.class)
    public void testInvalidFields() {
        CallDetailRecord cdr = CallDetailRecord.fromCsvLine("a,b,c,d,e,f,g,h,i,j,k,l,m");
        assertNotNull(cdr);
    }

    @Test
    public void testValidFields() {
        CallDetailRecord cdr = CallDetailRecord.fromCsvLine("a,b,1,d,5,f,g,h,i,j,1,2002,13");
        assertNotNull(cdr);
    }

    @Test
    public void testFromString() {
        CallDetailRecord expectedCdr = new CallDetailRecord("a", "b", 1L, "d", 5, "f", "g", "h", "i", "j",
                CallStatus.SUCCESS, StatusCode.OBD_SUCCESS_CALL_CONNECTED, 13);
        CallDetailRecord cdr = CallDetailRecord.fromCsvLine("a,b,1,d,5,f,g,h,i,j,1,1001,13");
        assertEquals(expectedCdr, cdr);
    }

    @Test
    public void testToString() {
        CallDetailRecord cdr = new CallDetailRecord("a", "b", 1L, "d", 5, "f", "g", "h", "i", "j",
                CallStatus.SUCCESS, StatusCode.OBD_SUCCESS_CALL_CONNECTED, 13);
        assertEquals("a,b,1,d,5,f,g,h,i,j,1,1001,13", cdr.toCsvLine());
    }
}
