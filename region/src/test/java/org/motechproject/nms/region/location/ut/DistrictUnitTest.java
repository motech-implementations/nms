package org.motechproject.nms.location.osgi.ut;

import org.junit.Before;
import org.junit.Test;
import org.motechproject.nms.region.location.domain.District;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class DistrictUnitTest {
    Validator validator;

    @Before
    public void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void testNameNull() {
        District district = new District();

        Set<ConstraintViolation<District>> constraintViolations = validator
                .validateProperty(district, "name");

        assertEquals(1, constraintViolations.size());
        assertEquals("may not be null", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void testNameSize() {
        District district = new District();
        district.setName("AAAAAAAAAABBBBBBBBBBAAAAAAAAAABBBBBBBBBBAAAAAAAAAABBBBBBBBBBAAAAAAAAAABBBBBBBBBBAAAAAAAAAABBBBBBBBBBX");

        Set<ConstraintViolation<District>> constraintViolations = validator
                .validateProperty(district, "name");

        assertEquals(1, constraintViolations.size());
        assertEquals("size must be between 1 and 100", constraintViolations.iterator().next().getMessage());

        district.setName("");

        constraintViolations = validator.validateProperty(district, "name");

        assertEquals(1, constraintViolations.size());
        assertEquals("size must be between 1 and 100", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void testRegionalNameNull() {
        District district = new District();

        Set<ConstraintViolation<District>> constraintViolations = validator
                .validateProperty(district, "regionalName");

        assertEquals(1, constraintViolations.size());
        assertEquals("may not be null", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void testRegionalNameSize() {
        District district = new District();
        district.setRegionalName("AAAAAAAAAABBBBBBBBBBAAAAAAAAAABBBBBBBBBBAAAAAAAAAABBBBBBBBBBAAAAAAAAAABBBBBBBBBBAAAAAAAAAABBBBBBBBBBX");

        Set<ConstraintViolation<District>> constraintViolations = validator
                .validateProperty(district, "regionalName");

        assertEquals(1, constraintViolations.size());
        assertEquals("size must be between 1 and 100", constraintViolations.iterator().next().getMessage());

        district.setRegionalName("");

        constraintViolations = validator.validateProperty(district, "regionalName");

        assertEquals(1, constraintViolations.size());
        assertEquals("size must be between 1 and 100", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void testCodeNull() {
        District district = new District();

        Set<ConstraintViolation<District>> constraintViolations = validator
                .validateProperty(district, "code");

        assertEquals(1, constraintViolations.size());
        assertEquals("may not be null", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void testStateNull() {
        District district = new District();

        Set<ConstraintViolation<District>> constraintViolations = validator
                .validateProperty(district, "state");

        assertEquals(1, constraintViolations.size());
        assertEquals("may not be null", constraintViolations.iterator().next().getMessage());
    }
}