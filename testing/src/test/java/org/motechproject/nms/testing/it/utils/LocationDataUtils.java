package org.motechproject.nms.testing.it.utils;

import org.motechproject.nms.region.domain.Circle;
import org.motechproject.nms.region.domain.District;
import org.motechproject.nms.region.domain.Language;
import org.motechproject.nms.region.domain.LanguageLocation;
import org.motechproject.nms.region.domain.State;

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
