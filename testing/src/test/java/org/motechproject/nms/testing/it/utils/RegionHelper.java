package org.motechproject.nms.testing.it.utils;

import org.motechproject.nms.region.domain.Circle;
import org.motechproject.nms.region.domain.District;
import org.motechproject.nms.region.domain.HealthBlock;
import org.motechproject.nms.region.domain.HealthFacility;
import org.motechproject.nms.region.domain.HealthFacilityType;
import org.motechproject.nms.region.domain.Language;
import org.motechproject.nms.region.domain.State;
import org.motechproject.nms.region.domain.Taluka;
import org.motechproject.nms.region.domain.Village;
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


    public District southDelhiDistrict() {
        District district = districtDataService.findById(3L);

        if (district == null) {
            district = new District();
            district.setName("South Delhi");
            district.setRegionalName("South Delhi");
            district.setCode(3L);
            district.setState(delhiState());
            district.setLanguage(hindiLanguage());
            districtDataService.create(district);
        }

        return district;
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

    public static Village createVillage(Taluka taluka, Long svid, Long vcode, String name) {
        Village village = new Village();
        village.setTaluka(taluka);
        village.setSvid(svid);
        village.setVcode(vcode);
        village.setName(name);
        village.setRegionalName(regionalName(name));
        return village;
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

    public static Language createLanguage(String code, String name, Circle circle, boolean defaultForCircle, District... districts) {
        Language language = new Language();
        language.setCode(code);
        language.setName(name);
        for (District district : districts) {
            district.setLanguage(language);
        }
        if (defaultForCircle) {
            circle.setDefaultLanguage(language);
        }

        return language;
    }

    public static Circle createCircle(String name) {
        return new Circle(name);
    }

    public static String regionalName(String name) {
        return String.format("regional name of %s", name);
    }

}
