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
        Circle delhiCircle = circleDataService.findByName("DE");

        if (delhiCircle == null) {
            delhiCircle = circleDataService.create(new Circle("DE"));
            delhiCircle.getStates().add(delhiState());
            circleDataService.update(delhiCircle);
        }

        return delhiCircle;
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


    public Language kannadaLanguage() {
        Language language = languageDataService.findByName("Kannada");
        if (language != null) {
            return language;
        }
        language = languageDataService.create(new Language("kn", "Kannada"));

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
