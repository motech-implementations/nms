package org.motechproject.nms.region.ut;

import org.junit.Before;
import org.junit.Test;
import org.motechproject.nms.region.domain.HealthBlock;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class HealthBlockUnitTest {
    Validator validator;

    @Before
    public void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void testNameNull() {
        HealthBlock healthBlock = new HealthBlock();

        Set<ConstraintViolation<HealthBlock>> constraintViolations = validator
                .validateProperty(healthBlock, "name");

        assertEquals(1, constraintViolations.size());
        assertEquals("may not be null", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void testNameSize() {
        HealthBlock healthBlock = new HealthBlock();
        healthBlock.setName("sfdsdfewfwefwefewfrweAAAAAAAAAABAAAAAAAAABBBBBBBBBBAAAAAAAAAABBBBBBBBBBAAAAAAAAAABBBBBBBBBBAAAAAAAAAABBBBBBBBBBXdsdsaasadsadasdasdasdasferferferfewrfed");

        Set<ConstraintViolation<HealthBlock>> constraintViolations = validator
                .validateProperty(healthBlock, "name");

        assertEquals(1, constraintViolations.size());
        assertEquals("size must be between 1 and 150", constraintViolations.iterator().next().getMessage());

        healthBlock.setName("");

        constraintViolations = validator.validateProperty(healthBlock, "name");

        assertEquals(1, constraintViolations.size());
        assertEquals("size must be between 1 and 150", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void testRegionalNameSize() {
        HealthBlock healthBlock = new HealthBlock();
        healthBlock.setRegionalName("sfdsdfewfwefwefewfrweAAAAAAAAAABAAAAAAAAABBBBBBBBBBAAAAAAAAAABBBBBBBBBBAAAAAAAAAABBBBBBBBBBAAAAAAAAAABBBBBBBBBBXdsdsaasadsadasdasdasdasferferferfewrfed");

        Set<ConstraintViolation<HealthBlock>> constraintViolations = validator
                .validateProperty(healthBlock, "regionalName");

        assertEquals(1, constraintViolations.size());
        assertEquals("size must be between 1 and 150", constraintViolations.iterator().next().getMessage());

        healthBlock.setRegionalName("");

        constraintViolations = validator.validateProperty(healthBlock, "regionalName");

        assertEquals(1, constraintViolations.size());
        assertEquals("size must be between 1 and 150", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void testHQSize() {
        HealthBlock healthBlock = new HealthBlock();
        healthBlock.setHq("AAAAAAAAAABBBBBBBBBBAAAAAAAAAABBBBBBBBBBAAAAAAAAAABBBBBBBBBBAAAAAAAAAABBBBBBBBBBAAAAAAAAAABBBBBBBBBBX");

        Set<ConstraintViolation<HealthBlock>> constraintViolations = validator
                .validateProperty(healthBlock, "hq");

        assertEquals(1, constraintViolations.size());
        assertEquals("size must be between 1 and 50", constraintViolations.iterator().next().getMessage());

        healthBlock.setHq("");

        constraintViolations = validator.validateProperty(healthBlock, "hq");

        assertEquals(1, constraintViolations.size());
        assertEquals("size must be between 1 and 50", constraintViolations.iterator().next().getMessage());
    }
    
    @Test
    public void testCodeNull() {
        HealthBlock healthBlock = new HealthBlock();

        Set<ConstraintViolation<HealthBlock>> constraintViolations = validator
                .validateProperty(healthBlock, "code");

        assertEquals(1, constraintViolations.size());
        assertEquals("may not be null", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void testTalukaNull() {
        HealthBlock healthBlock = new HealthBlock();

        Set<ConstraintViolation<HealthBlock>> constraintViolations = validator
                .validateProperty(healthBlock, "taluka");

        assertEquals(1, constraintViolations.size());
        assertEquals("may not be null", constraintViolations.iterator().next().getMessage());
    }}
