package org.motechproject.nms.testing.it.utils;

import org.motechproject.nms.region.domain.Circle;
import org.motechproject.nms.region.domain.District;
import org.motechproject.nms.region.domain.Language;
import org.motechproject.nms.region.domain.State;
import org.motechproject.nms.region.repository.CircleDataService;
import org.motechproject.nms.region.repository.DistrictDataService;
import org.motechproject.nms.region.repository.LanguageDataService;
import org.motechproject.nms.region.repository.StateDataService;

public class RegionHelper {
    private LanguageDataService languageDataService;
    private CircleDataService circleDataService;
    private DistrictDataService districtDataService;
    private StateDataService stateDataService;

    public RegionHelper(LanguageDataService languageDataService,
                              CircleDataService circleDataService,
                              StateDataService stateDataService,
                              DistrictDataService districtDataService) {

        this.languageDataService = languageDataService;
        this.circleDataService = circleDataService;
        this.districtDataService = districtDataService;
        this.stateDataService = stateDataService;
    }

    public Circle delhiCircle() {
        Circle c = circleDataService.findByName("DE");

        if (c == null) {
            c = circleDataService.create(new Circle("DE"));
            c.getStates().add(delhiState());
            c.setDefaultLanguage(hindiLanguage());
            circleDataService.update(c);
        }

        return c;
    }

    public Circle karnatakaCircle() {
        Circle c = circleDataService.findByName("KA");

        if (c == null) {
            c = circleDataService.create(new Circle("KA"));
            c.getStates().add(karnatakaState());
            circleDataService.update(c);
        }

        return c;
    }

    public State delhiState() {
        State state = stateDataService.findByCode(1l);

        if (state == null) {
            state = new State();
            state.setName("National Capital Territory of Delhi");
            state.setCode(1L);
            stateDataService.create(state);
        }

        return state;
    }


    public State karnatakaState() {
        State state = stateDataService.findByCode(2l);

        if (state == null) {
            state = new State();
            state.setName("Karnataka");
            state.setCode(2L);
            stateDataService.create(state);
        }

        return state;
    }


    public District newDelhiDistrict() {
        District district = districtDataService.findById(1L);

        if (district == null) {
            district = new District();
            district.setName("New Delhi");
            district.setRegionalName("New Delhi");
            district.setCode(1L);
            district.setState(delhiState());
            district.setLanguage(hindiLanguage());
            districtDataService.create(district);
        }

        return district;
    }


    public District southDelhiDistrict() {
        District district = districtDataService.findById(5L);

        if (district == null) {
            district = new District();
            district.setName("South Delhi");
            district.setRegionalName("South Delhi");
            district.setCode(5L);
            district.setState(delhiState());
            district.setLanguage(punjabiLanguage());
            districtDataService.create(district);
        }

        return district;
    }


    public District bangaloreDistrict() {
        District bangalore = districtDataService.findById(4L);
        if (bangalore == null) {
            bangalore = new District();
            bangalore.setName("Bengaluru");
            bangalore.setRegionalName("Bengaluru");
            bangalore.setCode(4L);
            bangalore.setState(karnatakaState());
            bangalore.setLanguage(tamilLanguage());
            districtDataService.create(bangalore);
        }

        return districtDataService.create(bangalore);
    }


    public District mysuruDistrict() {
        District district = districtDataService.findById(2L);
        if (district == null) {
            district = new District();
            district.setName("Mysuru");
            district.setRegionalName("Mysuru");
            district.setCode(2L);
            district.setState(karnatakaState());
            district.setLanguage(kannadaLanguage());
            districtDataService.create(district);
        }

        return districtDataService.create(district);
    }


    public Language tamilLanguage() {
        Language language = languageDataService.findByName("Tamil");
        if (language != null) {
            return language;
        }
        language = languageDataService.create(new Language("ta", "Tamil"));

        return language;
    }


    public Language kannadaLanguage() {
        Language language = languageDataService.findByName("Kannada");
        if (language != null) {
            return language;
        }
        language = languageDataService.create(new Language("kn", "Kannada"));

        return language;
    }


    public Language punjabiLanguage() {
        Language language = languageDataService.findByName("Punjabi");
        if (language != null) {
            return language;
        }
        language = languageDataService.create(new Language("pa", "Punjabi"));

        return language;
    }


    public Language hindiLanguage() {
        Language language = languageDataService.findByName("Hindi");
        if (language != null) {
            return language;
        }
        language = languageDataService.create(new Language("hi", "Hindi"));

        return language;
    }

    public String airtelOperator()
    {
        return "A";
    }
}
