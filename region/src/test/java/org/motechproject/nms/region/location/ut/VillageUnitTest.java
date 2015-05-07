package org.motechproject.nms.region.location.ut;

import org.junit.Before;
import org.junit.Test;
import org.motechproject.nms.region.location.domain.Taluka;
import org.motechproject.nms.region.location.domain.Village;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * Created by rob on 5/4/15.
 */
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
        village.setName("AAAAAAAAAABBBBBBBBBBAAAAAAAAAABBBBBBBBBBAAAAAAAAAABBBBBBBBBBAAAAAAAAAABBBBBBBBBBAAAAAAAAAABBBBBBBBBBX");

        Set<ConstraintViolation<Village>> constraintViolations = validator
                .validateProperty(village, "name");

        assertEquals(1, constraintViolations.size());
        assertEquals("size must be between 1 and 50", constraintViolations.iterator().next().getMessage());

        village.setName("");

        constraintViolations = validator.validateProperty(village, "name");

        assertEquals(1, constraintViolations.size());
        assertEquals("size must be between 1 and 50", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void testRegionalNameNull() {
        Village village = new Village();

        Set<ConstraintViolation<Village>> constraintViolations = validator
                .validateProperty(village, "regionalName");

        assertEquals(1, constraintViolations.size());
        assertEquals("may not be null", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void testRegionalNameToLong() {
        Village village = new Village();
        village.setRegionalName("AAAAAAAAAABBBBBBBBBBAAAAAAAAAABBBBBBBBBBAAAAAAAAAABBBBBBBBBBAAAAAAAAAABBBBBBBBBBAAAAAAAAAABBBBBBBBBBX");

        Set<ConstraintViolation<Village>> constraintViolations = validator
                .validateProperty(village, "regionalName");

        assertEquals(1, constraintViolations.size());
        assertEquals("size must be between 1 and 50", constraintViolations.iterator().next().getMessage());

        village.setRegionalName("");

        constraintViolations = validator.validateProperty(village, "regionalName");

        assertEquals(1, constraintViolations.size());
        assertEquals("size must be between 1 and 50", constraintViolations.iterator().next().getMessage());
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

        assertEquals(1, constraintViolations.size());
        assertEquals("At least one of vcode or svid must be set.", constraintViolations.iterator().next().getMessage());
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

        village.setVcode(null);
        village.setSvid(1L);

        constraintViolations = validator.validate(village);

        assertEquals(0, constraintViolations.size());

        village.setVcode(1L);
        village.setSvid(1L);

        constraintViolations = validator.validate(village);

        assertEquals(0, constraintViolations.size());
    }
}
