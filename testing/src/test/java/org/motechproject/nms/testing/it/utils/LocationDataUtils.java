package org.motechproject.nms.testing.it.utils;

import org.motechproject.nms.region.domain.Circle;
import org.motechproject.nms.region.domain.District;
import org.motechproject.nms.region.domain.Language;
import org.motechproject.nms.region.domain.State;

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
        return createDistrict(state, code, name, null);
    }

    public static District createDistrict(State state, Long code, String name, Language language) {
        District district = new District();
        district.setState(state);
        district.setCode(code);
        district.setName(name);
        district.setRegionalName(regionalName(name));
        district.setLanguage(language);
        return district;
    }

    public static Language createLanguage(String code, String name) {
        return new Language(code, name);
    }

    public static Circle createCircle(String name) {
        return new Circle(name);
    }

    public static String regionalName(String name) {
        return String.format("regional name of %s", name);
    }
}
