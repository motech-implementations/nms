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
        Circle circle = circleDataService.findByName("DE");
        if (circle != null) {
            return circle;
        }

        return circleDataService.create(new Circle("DE"));
    }

    public State delhiState() {
        State state = stateDataService.findByCode(1l);
        if (state != null) {
            return state;
        }

        state = new State();
        state.setName("National Capital Territory of Delhi");
        state.setCode(1L);

        return stateDataService.create(state);
    }

    public District newDelhiDistrict() {
        District district = districtDataService.findById(1L);
        if (district != null) {
            return district;
        }

        district = new District();
        district.setName("New Delhi");
        district.setRegionalName("New Delhi");
        district.setCode(1L);
        district.setState(delhiState());

        return districtDataService.create(district);
    }

    public Language hindiLanguage() {
        Language language = languageDataService.findByName("Hindi");
        if (language != null) {
            return language;
        }
        language = languageDataService.create(new Language("hi", "Hindi"));

        District district = newDelhiDistrict();
        district.setLanguage(language);
        districtDataService.update(district);

        Circle circle = delhiCircle();
        circle.getStates().add(district.getState());
        circleDataService.update(circle);

        return language;
    }
}
