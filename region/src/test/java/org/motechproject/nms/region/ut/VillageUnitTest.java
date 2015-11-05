package org.motechproject.nms.region.ut;

import org.junit.Before;
import org.junit.Test;
import org.motechproject.nms.region.domain.Taluka;
import org.motechproject.nms.region.domain.Village;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class VillageUnitTest {
    Validator validator;

    @Before
    public void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void testNameNull() {
        Village village = new Village();

        Set<ConstraintViolation<Village>> constraintViolations = validator
                .validateProperty(village, "name");

        assertEquals(1, constraintViolations.size());
        assertEquals("may not be null", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void testNameSize() {
        Village village = new Village();
        village.setName("AAAAAAAAAABBBBBBBBBBAAAAAAAAAABBBBBBBBBBAAAAAAAAAABBBBBBBBBBAAAAAAAAAABBBBBBBBBBAAAAAAAAAABBBBBBBBBBXAAAAAAAAAABBBBBBBBBBAAAAAAAAAABBBBBBBBBBAAAAAAAAAABBBBBBBBBBAAAAAAAAAABBBBBBBBBBAAAAAAAAAABBBBBBBBBBXJGDJFGEJWGFWEJHGFJWEHGFJWEGFJHEGfdewfwefwefeweeee");

        Set<ConstraintViolation<Village>> constraintViolations = validator
                .validateProperty(village, "name");

        assertEquals(1, constraintViolations.size());
        assertEquals("size must be between 1 and 250", constraintViolations.iterator().next().getMessage());

        village.setName("");

        constraintViolations = validator.validateProperty(village, "name");

        assertEquals(1, constraintViolations.size());
        assertEquals("size must be between 1 and 250", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void testRegionalNameToLong() {
        Village village = new Village();
        village.setRegionalName("AAAAAAAAAABBBBBBBBBBAAAAAAAAAABBBBBBBBBBAAAAAAAAAABBBBBBBBBBAAAAAAAAAABBBBBBBBBBAAAAAAAAAABBBBBBBBBBXAAAAAAAAAABBBBBBBBBBAAAAAAAAAABBBBBBBBBBAAAAAAAAAABBBBBBBBBBAAAAAAAAAABBBBBBBBBBAAAAAAAAAABBBBBBBBBBXJGDJFGEJWGFWEJHGFJWEHGFJWEGFJHEGfdewfwefwefeweeee");

        Set<ConstraintViolation<Village>> constraintViolations = validator
                .validateProperty(village, "regionalName");

        assertEquals(1, constraintViolations.size());
        assertEquals("size must be between 1 and 250", constraintViolations.iterator().next().getMessage());

        village.setRegionalName("");

        constraintViolations = validator.validateProperty(village, "regionalName");

        assertEquals(1, constraintViolations.size());
        assertEquals("size must be between 1 and 250", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void testTalukaNull() {
        Village village = new Village();

        Set<ConstraintViolation<Village>> constraintViolations = validator
                .validateProperty(village, "taluka");

        assertEquals(1, constraintViolations.size());
        assertEquals("may not be null", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void testVillageCodeInvalid() {
        Taluka taluka = new Taluka();
        taluka.setName("Taluka 1");
        taluka.setCode("0004");

        Village village = new Village();
        village.setName("Village");
        village.setRegionalName("Village");
        village.setTaluka(taluka);

        Set<ConstraintViolation<Village>> constraintViolations = validator.validate(village);

        assertEquals(2, constraintViolations.size()); // We get the same message twice...
        for (ConstraintViolation<Village> constraintViolation : constraintViolations) {
            assertEquals("At least one of vcode or svid must be set.", constraintViolation.getMessage());
        }
    }

    @Test
    public void testVillageCodeValid() {
        Taluka taluka = new Taluka();
        taluka.setName("Taluka 1");
        taluka.setCode("0004");

        Village village = new Village();
        village.setName("Village");
        village.setRegionalName("Village");
        village.setTaluka(taluka);

        village.setVcode(1L);

        Set<ConstraintViolation<Village>> constraintViolations = validator.validate(village);

        assertEquals(0, constraintViolations.size());

        village.setVcode(0);
        village.setSvid(1L);

        constraintViolations = validator.validate(village);

        assertEquals(0, constraintViolations.size());

        village.setVcode(1L);
        village.setSvid(1L);

        constraintViolations = validator.validate(village);

        assertEquals(0, constraintViolations.size());
    }
}
