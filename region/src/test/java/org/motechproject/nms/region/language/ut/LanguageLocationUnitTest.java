package org.motechproject.nms.region.language.ut;

import org.junit.Before;
import org.junit.Test;
import org.motechproject.nms.region.circle.domain.Circle;
import org.motechproject.nms.region.language.domain.Language;
import org.motechproject.nms.region.language.domain.LanguageLocation;
import org.motechproject.nms.region.location.domain.District;
import org.motechproject.nms.region.location.domain.State;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Created by rob on 5/6/15.
 */
public class LanguageLocationUnitTest {
    Validator validator;

    @Before
    public void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void testCodeNull() {
        LanguageLocation languageLocation = new LanguageLocation();

        Set<ConstraintViolation<LanguageLocation>> constraintViolations = validator
                .validateProperty(languageLocation, "code");

        assertEquals(1, constraintViolations.size());
        assertEquals("may not be null", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void testLanguageNull() {
        LanguageLocation languageLocation = new LanguageLocation();

        Set<ConstraintViolation<LanguageLocation>> constraintViolations = validator
                .validateProperty(languageLocation, "language");

        assertEquals(1, constraintViolations.size());
        assertEquals("may not be null", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void testCircleNull() {
        LanguageLocation languageLocation = new LanguageLocation();

        Set<ConstraintViolation<LanguageLocation>> constraintViolations = validator
                .validateProperty(languageLocation, "circle");

        assertEquals(1, constraintViolations.size());
        assertEquals("may not be null", constraintViolations.iterator().next().getMessage());
    }


    @Test
    public void testLanguageLocationMissingStateAndDistricts() {
        LanguageLocation languageLocation = new LanguageLocation();

        languageLocation.setCode("AA");
        languageLocation.setLanguage(new Language());
        languageLocation.setCircle(new Circle());

        Set<ConstraintViolation<LanguageLocation>> constraintViolations = validator.validate(languageLocation);

        assertEquals(1, constraintViolations.size());
        assertEquals("At least one of state or district must be set.", constraintViolations.iterator().next()
                .getMessage());
    }

    @Test
    public void testLanguageLocationBothStateAndDistrictInvalid() {
        LanguageLocation languageLocation = new LanguageLocation();

        languageLocation.setCode("AA");
        languageLocation.setLanguage(new Language());
        languageLocation.setCircle(new Circle());
        languageLocation.setState(new State());
        languageLocation.getDistricts().add(new District());

        Set<ConstraintViolation<LanguageLocation>> constraintViolations = validator.validate(languageLocation);

        assertEquals(1, constraintViolations.size());
        assertEquals("At least one of state or district must be set.", constraintViolations.iterator().next()
                .getMessage());
    }

    @Test
    public void testLanguageLocationValid() {
        LanguageLocation languageLocation = new LanguageLocation();

        languageLocation.setCode("AA");
        languageLocation.setLanguage(new Language());
        languageLocation.setCircle(new Circle());
        languageLocation.setState(new State());

        Set<ConstraintViolation<LanguageLocation>> constraintViolations = validator.validate(languageLocation);

        assertEquals(0, constraintViolations.size());

        languageLocation.setState(null);
        languageLocation.getDistricts().add(new District());

        constraintViolations = validator.validate(languageLocation);

        assertEquals(0, constraintViolations.size());
    }

    @Test
    public void testGetStateWithStateSet() {
        LanguageLocation languageLocation = new LanguageLocation();

        languageLocation.setCode("AA");
        languageLocation.setLanguage(new Language());
        languageLocation.setCircle(new Circle());
        languageLocation.setState(new State("New Jersey", 1L));

        assertEquals((Long)1L, languageLocation.getState().getCode());
    }

    @Test
    public void testGetStateWithDistrictsInSingleState() {
        LanguageLocation languageLocation = new LanguageLocation();

        languageLocation.setCode("AA");
        languageLocation.setLanguage(new Language());
        languageLocation.setCircle(new Circle());

        State nj = new State("New Jersey", 1L);
        District d1 = new District();
        d1.setCode(1L);
        d1.setState(nj);
        languageLocation.getDistricts().add(d1);

        District d2 = new District();
        d2.setCode(2L);
        d2.setState(nj);
        languageLocation.getDistricts().add(d2);

        assertEquals((Long)1L, languageLocation.getState().getCode());
    }

    @Test
    public void testGetStateWithDistrictsInMultipleStates() {
        LanguageLocation languageLocation = new LanguageLocation();

        languageLocation.setCode("AA");
        languageLocation.setLanguage(new Language());
        languageLocation.setCircle(new Circle());

        State nj = new State("New Jersey", 1L);
        District d1 = new District();
        d1.setCode(1L);
        d1.setState(nj);
        languageLocation.getDistricts().add(d1);

        State wa = new State("Washington", 2L);
        District d2 = new District();
        d2.setCode(2L);
        d2.setState(wa);
        languageLocation.getDistricts().add(d2);

        assertNull(languageLocation.getState());
    }
}

