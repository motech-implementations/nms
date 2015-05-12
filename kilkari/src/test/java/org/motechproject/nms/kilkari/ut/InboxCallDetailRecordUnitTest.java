package org.motechproject.nms.kilkari.ut;

import org.junit.Before;
import org.junit.Test;
import org.motechproject.nms.kilkari.domain.InboxCallDetailRecord;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class InboxCallDetailRecordUnitTest {
    Validator validator;

    @Before
    public void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void testCallingNumberTooShort() {
        InboxCallDetailRecord icd = new InboxCallDetailRecord();
        icd.setCallingNumber(111111111L);

        Set<ConstraintViolation<InboxCallDetailRecord>> constraintViolations = validator
                .validateProperty(icd, "callingNumber");

        assertEquals(1, constraintViolations.size());
        assertEquals("callingNumber must be 10 digits", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void testCallingNumberTooLong() {
        InboxCallDetailRecord icd = new InboxCallDetailRecord();
        icd.setCallingNumber(11111111111L);

        Set<ConstraintViolation<InboxCallDetailRecord>> constraintViolations = validator
                .validateProperty(icd, "callingNumber");

        assertEquals(1, constraintViolations.size());
        assertEquals("callingNumber must be 10 digits", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void testCallingNumberValid() {
        InboxCallDetailRecord icd = new InboxCallDetailRecord();
        icd.setCallingNumber(1111111111L);

        Set<ConstraintViolation<InboxCallDetailRecord>> constraintViolations = validator
                .validateProperty(icd, "callingNumber");

        assertEquals(0, constraintViolations.size());
    }
}
