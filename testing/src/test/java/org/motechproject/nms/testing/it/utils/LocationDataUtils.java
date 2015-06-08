package org.motechproject.nms.testing.it.utils;

import org.motechproject.nms.region.domain.Circle;
import org.motechproject.nms.region.domain.District;
import org.motechproject.nms.region.domain.HealthBlock;
import org.motechproject.nms.region.domain.HealthFacility;
import org.motechproject.nms.region.domain.HealthFacilityType;
import org.motechproject.nms.region.domain.Language;
import org.motechproject.nms.region.domain.LanguageLocation;
import org.motechproject.nms.region.domain.State;
import org.motechproject.nms.region.domain.Taluka;

import java.util.Arrays;

public final class LocationDataUtils {
    private LocationDataUtils() {
    }

    public static State createState(Long code, String name) {
        State state = new State();
        state.setCode(code);
        state.setName(name);
        return state;
    }

    public static District createDistrict(State state, Long code, String name) {
        District district = new District();
        district.setState(state);
        district.setCode(code);
        district.setName(name);
        district.setRegionalName(regionalName(name));
        return district;
    }

    public static Taluka createTaluka(District district, String code, String name, int identity) {
        Taluka taluka = new Taluka();
        taluka.setDistrict(district);
        taluka.setCode(code);
        taluka.setName(name);
        taluka.setRegionalName(regionalName(name));
        taluka.setIdentity(identity);
        return taluka;
    }

    public static HealthBlock createHealthBlock(Taluka taluka, Long code, String name, String hq) {
        HealthBlock healthBlock = new HealthBlock();
        healthBlock.setTaluka(taluka);
        healthBlock.setCode(code);
        healthBlock.setName(name);
        healthBlock.setRegionalName(regionalName(name));
        healthBlock.setHq(hq);
        return healthBlock;
    }

    public static HealthFacility createHealthFacility(HealthBlock healthBlock, Long code, String name, HealthFacilityType type) {
        HealthFacility healthFacility = new HealthFacility();
        healthFacility.setHealthBlock(healthBlock);
        healthFacility.setCode(code);
        healthFacility.setName(name);
        healthFacility.setRegionalName(regionalName(name));
        healthFacility.setHealthFacilityType(type);
        return healthFacility;
    }

    public static HealthFacilityType createHealthFacilityType(String name, Long code) {
        HealthFacilityType healthFacilityType = new HealthFacilityType();
        healthFacilityType.setName(name);
        healthFacilityType.setCode(code);
        return healthFacilityType;
    }


    public static Language createLanguage(String name) {
        return new Language(name);
    }

    public static LanguageLocation createLanguageLocation(String code, Language language, Circle circle, boolean defaultForCircle, District... districts) {
        LanguageLocation languageLocation = new LanguageLocation();
        languageLocation.setCode(code);
        languageLocation.setLanguage(language);
        languageLocation.setCircle(circle);
        languageLocation.setDefaultForCircle(defaultForCircle);
        languageLocation.getDistrictSet().addAll(Arrays.asList(districts));
        return languageLocation;
    }

    public static Circle createCircle(String name) {
        return new Circle(name);
    }

    public static String regionalName(String name) {
        return String.format("regional name of %s", name);
    }
}
