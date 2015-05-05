package org.motechproject.nms.location.osgi.ut;

import org.junit.Before;
import org.junit.Test;
import org.motechproject.nms.location.domain.HealthFacility;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class HealthFacilityUnitTest {
    Validator validator;

    @Before
    public void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void testNameNull() {
        HealthFacility healthFacility = new HealthFacility();

        Set<ConstraintViolation<HealthFacility>> constraintViolations = validator
                .validateProperty(healthFacility, "name");

        assertEquals(1, constraintViolations.size());
        assertEquals("may not be null", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void testNameSize() {
        HealthFacility healthFacility = new HealthFacility();
        healthFacility
                .setName("AAAAAAAAAABBBBBBBBBBAAAAAAAAAABBBBBBBBBBAAAAAAAAAABBBBBBBBBBAAAAAAAAAABBBBBBBBBBAAAAAAAAAABBBBBBBBBBX");

        Set<ConstraintViolation<HealthFacility>> constraintViolations = validator
                .validateProperty(healthFacility, "name");

        assertEquals(1, constraintViolations.size());
        assertEquals("size must be between 1 and 50", constraintViolations.iterator().next().getMessage());

        healthFacility.setName("");

        constraintViolations = validator.validateProperty(healthFacility, "name");

        assertEquals(1, constraintViolations.size());
        assertEquals("size must be between 1 and 50", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void testRegionalNameNull() {
        HealthFacility healthFacility = new HealthFacility();

        Set<ConstraintViolation<HealthFacility>> constraintViolations = validator
                .validateProperty(healthFacility, "regionalName");

        assertEquals(1, constraintViolations.size());
        assertEquals("may not be null", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void testRegionalNameSize() {
        HealthFacility healthFacility = new HealthFacility();
        healthFacility
                .setRegionalName("AAAAAAAAAABBBBBBBBBBAAAAAAAAAABBBBBBBBBBAAAAAAAAAABBBBBBBBBBAAAAAAAAAABBBBBBBBBBAAAAAAAAAABBBBBBBBBBX");

        Set<ConstraintViolation<HealthFacility>> constraintViolations = validator
                .validateProperty(healthFacility, "regionalName");

        assertEquals(1, constraintViolations.size());
        assertEquals("size must be between 1 and 50", constraintViolations.iterator().next().getMessage());

        healthFacility.setRegionalName("");

        constraintViolations = validator.validateProperty(healthFacility, "regionalName");

        assertEquals(1, constraintViolations.size());
        assertEquals("size must be between 1 and 50", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void testCodeNull() {
        HealthFacility healthFacility = new HealthFacility();

        Set<ConstraintViolation<HealthFacility>> constraintViolations = validator
                .validateProperty(healthFacility, "code");

        assertEquals(1, constraintViolations.size());
        assertEquals("may not be null", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void testHealthFacilityTypeNull() {
        HealthFacility healthFacility = new HealthFacility();

        Set<ConstraintViolation<HealthFacility>> constraintViolations = validator
                .validateProperty(healthFacility, "healthFacilityType");

        assertEquals(1, constraintViolations.size());
        assertEquals("may not be null", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void testHealthBlockNull() {
        HealthFacility healthFacility = new HealthFacility();

        Set<ConstraintViolation<HealthFacility>> constraintViolations = validator
                .validateProperty(healthFacility, "healthBlock");

        assertEquals(1, constraintViolations.size());
        assertEquals("may not be null", constraintViolations.iterator().next().getMessage());
    }
}
