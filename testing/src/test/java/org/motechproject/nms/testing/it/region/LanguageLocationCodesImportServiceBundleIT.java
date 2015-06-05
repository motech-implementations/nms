package org.motechproject.nms.testing.it.region;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.nms.csv.exception.CsvImportDataException;
import org.motechproject.nms.region.domain.Circle;
import org.motechproject.nms.region.domain.District;
import org.motechproject.nms.region.domain.Language;
import org.motechproject.nms.region.domain.LanguageLocation;
import org.motechproject.nms.region.domain.State;
import org.motechproject.nms.region.repository.CircleDataService;
import org.motechproject.nms.region.repository.DistrictDataService;
import org.motechproject.nms.region.repository.HealthBlockDataService;
import org.motechproject.nms.region.repository.HealthFacilityDataService;
import org.motechproject.nms.region.repository.HealthFacilityTypeDataService;
import org.motechproject.nms.region.repository.HealthSubFacilityDataService;
import org.motechproject.nms.region.repository.LanguageDataService;
import org.motechproject.nms.region.repository.LanguageLocationDataService;
import org.motechproject.nms.region.repository.NationalDefaultLanguageDataService;
import org.motechproject.nms.region.repository.StateDataService;
import org.motechproject.nms.region.repository.TalukaDataService;
import org.motechproject.nms.region.repository.VillageDataService;
import org.motechproject.nms.region.service.LanguageLocationImportService;
import org.motechproject.testing.osgi.BasePaxIT;
import org.motechproject.testing.osgi.container.MotechNativeTestContainerFactory;
import org.ops4j.pax.exam.ExamFactory;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;

import javax.inject.Inject;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@ExamFactory(MotechNativeTestContainerFactory.class)
public class LanguageLocationCodesImportServiceBundleIT extends BasePaxIT {

    @Inject
    private NationalDefaultLanguageDataService nationalDefaultLanguageLocationDataService;
    @Inject
    private LanguageDataService languageDataService;
    @Inject
    private StateDataService stateDataService;
    @Inject
    private DistrictDataService districtDataService;
    @Inject
    private TalukaDataService talukaDataService;
    @Inject
    private VillageDataService villageDataService;
    @Inject
    private HealthBlockDataService healthBlockDataService;
    @Inject
    private HealthFacilityTypeDataService healthFacilityTypeDataService;
    @Inject
    private HealthFacilityDataService healthFacilityDataService;
    @Inject
    private HealthSubFacilityDataService healthSubFacilityDataService;
    @Inject
    private CircleDataService circleDataService;

    @Inject
    private LanguageLocationImportService languageLocationImportService;

    @Before
    public void setUp() {
        nationalDefaultLanguageLocationDataService.deleteAll();
        languageDataService.deleteAll();
        healthSubFacilityDataService.deleteAll();
        healthFacilityDataService.deleteAll();
        healthFacilityTypeDataService.deleteAll();
        healthBlockDataService.deleteAll();
        villageDataService.deleteAll();
        talukaDataService.deleteAll();
        districtDataService.deleteAll();
        stateDataService.deleteAll();
        circleDataService.deleteAll();

        State state1 = createState(1L, "State 1");
        District district11 = createDistrict(state1, 11L, "District 11");
        District district12 = createDistrict(state1, 12L, "District 12");
        state1.getDistricts().addAll(Arrays.asList(district11, district12));
        stateDataService.create(state1);

        State state2 = createState(2L, "State 2");
        District district21 = createDistrict(state2, 21L, "District 21");
        state2.getDistricts().addAll(Collections.singletonList(district21));
        districtDataService.create(district21);

        State state3 = createState(3L, "State 3");
        District district31 = createDistrict(state3, 31L, "District 31");
        District district32 = createDistrict(state3, 32L, "District 32");
        state3.getDistricts().addAll(Arrays.asList(district31, district32));
        stateDataService.create(state3);

        State state4 = createState(4L, "State 4");
        District district41 = createDistrict(state4, 41L, "District 41");
        District district42 = createDistrict(state4, 42L, "District 42");
        state4.getDistricts().addAll(Arrays.asList(district41, district42));
        stateDataService.create(state3);

        Language lang1 = createLanguage("Lang 1");
        languageDataService.create(lang1);

        Language lang2 = createLanguage("Lang 2");
        languageDataService.create(lang2);

        Circle circle1 = createCircle("Circle 1");
        circle1.getStates().addAll(Arrays.asList(state1, state2));
        circleDataService.create(circle1);

        Circle circle2 = createCircle("Circle 2");
        circle2.getStates().addAll(Arrays.asList(state3));
        circleDataService.create(circle2);

        Circle circle3 = createCircle("Circle 3");
        LanguageLocation llc31 = createLanguageLocation("LLC 31", lang1, circle3, false, district32);
        circle3.getStates().addAll(Arrays.asList(state3));
        circle3.getLanguageLocations().addAll(Arrays.asList(llc31));
        circleDataService.create(circle3);

        Circle circle4 = createCircle("Circle 4");
        LanguageLocation llc41 = createLanguageLocation("LLC 41", lang1, circle4, true, district41);
        circle4.getStates().addAll(Arrays.asList(state4));
        circle4.getLanguageLocations().addAll(Arrays.asList(llc41));
        circleDataService.create(circle4);
    }

    @Test
    public void testImportWhenStateAndDistrictPresent() throws Exception {
        Reader reader = createReaderWithHeaders("LLC 0,Lang 1,Circle 1,State 1,District 11,N");
        languageLocationImportService.importData(reader);

        District district11 = districtDataService.findByCode(11L);
        assertLanguageLocationCode(district11.getLanguageLocation(), "LLC 0", "Lang 1");
    }

    @Test
    public void testImportWhenOnlyStatePresent() throws Exception {
        Reader reader = createReaderWithHeaders("LLC 0,Lang 1,Circle 1,State 1,,N");
        languageLocationImportService.importData(reader);

        District district11 = districtDataService.findByCode(11L);
        assertLanguageLocationCode(district11.getLanguageLocation(), "LLC 0", "Lang 1");

        District district12 = districtDataService.findByCode(12L);
        assertLanguageLocationCode(district12.getLanguageLocation(), "LLC 0", "Lang 1");
    }

    @Test
    public void testImportWhenOnlyDistrictPresent() throws Exception {
        Reader reader = createReaderWithHeaders("LLC 0,Lang 1,Circle 1,,District 11,N");
        languageLocationImportService.importData(reader);

        District district11 = districtDataService.findByCode(11L);
        assertLanguageLocationCode(district11.getLanguageLocation(), "LLC 0", "Lang 1");
    }

    @Test
    public void testImportWhenLanguageLocationCodeExists() throws Exception {
        Reader reader = createReaderWithHeaders("LLC 31,Lang 1,Circle 3,State 3,District 31,N");
        languageLocationImportService.importData(reader);

        District district32 = districtDataService.findByCode(31L);
        assertLanguageLocationCode(district32.getLanguageLocation(), "LLC 31", "Lang 1");
    }

    @Test(expected = CsvImportDataException.class)
    public void testImportWhenStateAndDistrictAreNull() throws Exception {
        Reader reader = createReaderWithHeaders("LLC 0,Lang 1,Circle 1,,,N");
        languageLocationImportService.importData(reader);
    }

    @Test(expected = CsvImportDataException.class)
    public void testImportWhenDistrictNotContainedInCircle() throws Exception {
        Reader reader = createReaderWithHeaders("LLC 0,Lang 1,Circle 1,State 3,District 31,N");
        languageLocationImportService.importData(reader);
    }

    @Test(expected = CsvImportDataException.class)
    public void testImportWhenLanguageLocationCodeAlreadySetForDistrict() throws Exception {
        Reader reader = createReaderWithHeaders("LLC 0,Lang 1,Circle 4,State 4,District 41,N");
        languageLocationImportService.importData(reader);
    }

    @Test(expected = CsvImportDataException.class)
    public void testImportWhenDistrictStateDoesNotMatch() throws Exception {
        Reader reader = createReaderWithHeaders("LLC 0,Lang 1,Circle 1,State 1,District 21,N");
        languageLocationImportService.importData(reader);
    }

    @Test(expected = CsvImportDataException.class)
    public void testImportWhenLanguageLocationCodeExistsAndLanguageDoesNotMatch() throws Exception {
        Reader reader = createReaderWithHeaders("LLC 31,Lang 2,Circle 3,State 3,District 31,N");
        languageLocationImportService.importData(reader);
    }

    @Test(expected = CsvImportDataException.class)
    public void testImportWhenLanguageLocationCodeExistsAndCircleDoesNotMatch() throws Exception {
        Reader reader = createReaderWithHeaders("LLC 31,Lang 1,Circle 2,State 3,District 31,N");
        languageLocationImportService.importData(reader);
    }

    @Test(expected = CsvImportDataException.class)
    public void testImportWhenLanguageLocationCodeExistsAndDefaultForCircleDoesNotMatch() throws Exception {
        Reader reader = createReaderWithHeaders("LLC 31,Lang 1,Circle 3,State 3,District 31,Y");
        languageLocationImportService.importData(reader);
    }

    @Test(expected = CsvImportDataException.class)
    public void testImportWhenLanguageLocationCodeNotExistsAndIsDefaultForCircleButNotUnique() throws Exception {
        Reader reader = createReaderWithHeaders("LLC 0,Lang 1,Circle 4,State 4,District 42,Y");
        languageLocationImportService.importData(reader);
    }

    private State createState(Long code, String name) {
        State state = new State();
        state.setCode(code);
        state.setName(name);
        return state;
    }

    private District createDistrict(State state, Long code, String name) {
        District district = new District();
        district.setState(state);
        district.setCode(code);
        district.setName(name);
        district.setRegionalName(regionalName(name));
        return district;
    }

    private Language createLanguage(String name) {
        return new Language(name);
    }

    private LanguageLocation createLanguageLocation(String code, Language language, Circle circle, boolean defaultForCircle, District... districts) {
        LanguageLocation languageLocation = new LanguageLocation();
        languageLocation.setCode(code);
        languageLocation.setLanguage(language);
        languageLocation.setCircle(circle);
        languageLocation.setDefaultForCircle(defaultForCircle);
        languageLocation.getDistrictSet().addAll(Arrays.asList(districts));
        return languageLocation;
    }

    private Circle createCircle(String name) {
        return new Circle(name);
    }

    private String regionalName(String name) {
        return String.format("regional name of %s", name);
    }

    private Reader createReaderWithHeaders(String... lines) {
        StringBuilder builder = new StringBuilder();
        builder.append("languagelocation code,Language,Circle,State,District,Default Language for Circle (Y/N)").append("\n");
        for (String line : lines) {
            builder.append(line).append("\n");
        }
        return new StringReader(builder.toString());
    }

    private void assertLanguageLocationCode(LanguageLocation languageLocation, String code, String language) {
        assertNotNull(languageLocation);
        assertEquals(code, languageLocation.getCode());
        assertNotNull(languageLocation.getLanguage());
        assertEquals(language, languageLocation.getLanguage().getName());
    }
}
