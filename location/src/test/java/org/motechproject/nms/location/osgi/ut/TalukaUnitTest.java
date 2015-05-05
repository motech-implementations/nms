package org.motechproject.nms.location.osgi.ut;

import org.junit.Before;
import org.junit.Test;
import org.motechproject.nms.location.domain.Taluka;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * Created by rob on 5/4/15.
 */
public class TalukaUnitTest {
    Validator validator;

    @Before
    public void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void testNameNull() {
        Taluka taluka = new Taluka();

        Set<ConstraintViolation<Taluka>> constraintViolations = validator
                .validateProperty(taluka, "name");

        assertEquals(1, constraintViolations.size());
        assertEquals("may not be null", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void testNameSize() {
        Taluka taluka = new Taluka();
        taluka.setName("AAAAAAAAAABBBBBBBBBBAAAAAAAAAABBBBBBBBBBAAAAAAAAAABBBBBBBBBBAAAAAAAAAABBBBBBBBBBAAAAAAAAAABBBBBBBBBBX");

        Set<ConstraintViolation<Taluka>> constraintViolations = validator
                .validateProperty(taluka, "name");

        assertEquals(1, constraintViolations.size());
        assertEquals("size must be between 1 and 100", constraintViolations.iterator().next().getMessage());

        taluka.setName("");

        constraintViolations = validator.validateProperty(taluka, "name");

        assertEquals(1, constraintViolations.size());
        assertEquals("size must be between 1 and 100", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void testRegionalNameNull() {
        Taluka taluka = new Taluka();

        Set<ConstraintViolation<Taluka>> constraintViolations = validator
                .validateProperty(taluka, "regionalName");

        assertEquals(1, constraintViolations.size());
        assertEquals("may not be null", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void testRegionalNameSize() {
        Taluka taluka = new Taluka();
        taluka.setRegionalName("AAAAAAAAAABBBBBBBBBBAAAAAAAAAABBBBBBBBBBAAAAAAAAAABBBBBBBBBBAAAAAAAAAABBBBBBBBBBAAAAAAAAAABBBBBBBBBBX");

        Set<ConstraintViolation<Taluka>> constraintViolations = validator
                .validateProperty(taluka, "regionalName");

        assertEquals(1, constraintViolations.size());
        assertEquals("size must be between 1 and 100", constraintViolations.iterator().next().getMessage());

        taluka.setRegionalName("");

        constraintViolations = validator.validateProperty(taluka, "regionalName");

        assertEquals(1, constraintViolations.size());
        assertEquals("size must be between 1 and 100", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void testCodeNull() {
        Taluka taluka = new Taluka();

        Set<ConstraintViolation<Taluka>> constraintViolations = validator
                .validateProperty(taluka, "code");

        assertEquals(1, constraintViolations.size());
        assertEquals("may not be null", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void testCodeSize() {
        Taluka taluka = new Taluka();
        taluka.setCode("00000001");

        Set<ConstraintViolation<Taluka>> constraintViolations = validator
                .validateProperty(taluka, "code");

        assertEquals(1, constraintViolations.size());
        assertEquals("size must be between 1 and 7", constraintViolations.iterator().next().getMessage());

        taluka.setCode("");

        constraintViolations = validator.validateProperty(taluka, "code");

        assertEquals(1, constraintViolations.size());
        assertEquals("size must be between 1 and 7", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void testIdentityNull() {
        Taluka taluka = new Taluka();

        Set<ConstraintViolation<Taluka>> constraintViolations = validator
                .validateProperty(taluka, "identity");

        assertEquals(1, constraintViolations.size());
        assertEquals("may not be null", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void testDistrictNull() {
        Taluka taluka = new Taluka();

        Set<ConstraintViolation<Taluka>> constraintViolations = validator
                .validateProperty(taluka, "district");

        assertEquals(1, constraintViolations.size());
        assertEquals("may not be null", constraintViolations.iterator().next().getMessage());
    }
}
