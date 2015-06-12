package org.motechproject.nms.testing.it.region;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.nms.csv.exception.CsvImportDataException;
import org.motechproject.nms.region.domain.Circle;
import org.motechproject.nms.region.domain.District;
import org.motechproject.nms.region.domain.Language;
import org.motechproject.nms.region.domain.State;
import org.motechproject.nms.region.repository.CircleDataService;
import org.motechproject.nms.region.repository.DistrictDataService;
import org.motechproject.nms.region.repository.LanguageDataService;
import org.motechproject.nms.region.repository.StateDataService;
import org.motechproject.nms.region.service.LanguageLocationImportService;
import org.motechproject.nms.testing.service.TestingService;
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
import static org.motechproject.nms.testing.it.utils.RegionHelper.createDistrict;
import static org.motechproject.nms.testing.it.utils.RegionHelper.createState;
import static org.motechproject.nms.testing.it.utils.RegionHelper.createCircle;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@ExamFactory(MotechNativeTestContainerFactory.class)
public class LanguageLocationCodesImportServiceBundleIT extends BasePaxIT {

    @Inject
    private TestingService testingService;
    @Inject
    private LanguageDataService languageDataService;
    @Inject
    private StateDataService stateDataService;
    @Inject
    private DistrictDataService districtDataService;
    @Inject
    private CircleDataService circleDataService;
    @Inject
    private LanguageLocationImportService languageLocationImportService;

    @Before
    public void setUp() {
        testingService.clearDatabase();

        Language lang1 = new Language("L1", "Lang 1");
        languageDataService.create(lang1);

        Language lang2 = new Language("L2", "Lang 2");
        languageDataService.create(lang2);

        State state1 = createState(1L, "State 1");
        District district11 = createDistrict(state1, 11L, "District 11", null);
        District district12 = createDistrict(state1, 12L, "District 12", null);
        state1.getDistricts().addAll(Arrays.asList(district11, district12));
        stateDataService.create(state1);

        State state2 = createState(2L, "State 2");
        District district21 = createDistrict(state2, 21L, "District 21", null);
        state2.getDistricts().addAll(Collections.singletonList(district21));
        districtDataService.create(district21);

        State state3 = createState(3L, "State 3");
        District district31 = createDistrict(state3, 31L, "District 31", null);
        District district32 = createDistrict(state3, 32L, "District 32", lang1);
        state3.getDistricts().addAll(Arrays.asList(district31, district32));
        stateDataService.create(state3);

        State state4 = createState(4L, "State 4");
        District district41 = createDistrict(state4, 41L, "District 41", lang1);
        District district42 = createDistrict(state4, 42L, "District 42", null);
        state4.getDistricts().addAll(Arrays.asList(district41, district42));
        stateDataService.create(state4);

        Circle circle1 = createCircle("Circle 1");
        circle1.getStates().addAll(Arrays.asList(state1, state2));
        circleDataService.create(circle1);

        Circle circle2 = createCircle("Circle 2");
        circle2.getStates().addAll(Collections.singletonList(state3));
        circleDataService.create(circle2);

        Circle circle3 = createCircle("Circle 3");
        circle3.getStates().addAll(Collections.singletonList(state3));
        circleDataService.create(circle3);

        Circle circle4 = createCircle("Circle 4");
        circle4.setDefaultLanguage(lang1);
        circle4.getStates().addAll(Collections.singletonList(state4));
        circleDataService.create(circle4);
    }

    @Test
    public void testImportWhenStateAndDistrictPresent() throws Exception {
        Reader reader = createReaderWithHeaders("L1,Lang 1,Circle 1,State 1,District 11,N");
        languageLocationImportService.importData(reader);

        District district11 = districtDataService.findByCode(11L);
        assertLanguageCode(district11.getLanguage(), "L1", "Lang 1");
    }

    @Test
    public void testImportWhenOnlyStatePresent() throws Exception {
        Reader reader = createReaderWithHeaders("L1,Lang 1,Circle 1,State 1,,N");
        languageLocationImportService.importData(reader);

        District district11 = districtDataService.findByCode(11L);
        assertLanguageCode(district11.getLanguage(), "L1", "Lang 1");

        District district12 = districtDataService.findByCode(12L);
        assertLanguageCode(district12.getLanguage(), "L1", "Lang 1");
    }

    @Test
    public void testImportWhenOnlyDistrictPresent() throws Exception {
        Reader reader = createReaderWithHeaders("L1,Lang 1,Circle 1,,District 11,N");
        languageLocationImportService.importData(reader);

        District district11 = districtDataService.findByCode(11L);
        assertLanguageCode(district11.getLanguage(), "L1", "Lang 1");
    }

    @Test
    public void testImportWhenLanguageCodeExists() throws Exception {
        Reader reader = createReaderWithHeaders("L1,Lang 1,Circle 3,State 3,District 31,N");
        languageLocationImportService.importData(reader);

        District district32 = districtDataService.findByCode(31L);
        assertLanguageCode(district32.getLanguage(), "L1", "Lang 1");
    }

    @Test(expected = CsvImportDataException.class)
    public void testImportWhenStateAndDistrictAreNull() throws Exception {
        Reader reader = createReaderWithHeaders("L1,Lang 1,Circle 1,,,N");
        languageLocationImportService.importData(reader);
    }

    @Test(expected = CsvImportDataException.class)
    public void testImportWhenDistrictNotContainedInCircle() throws Exception {
        Reader reader = createReaderWithHeaders("L1,Lang 1,Circle 1,State 3,District 31,N");
        languageLocationImportService.importData(reader);
    }

    @Test(expected = CsvImportDataException.class)
    public void testImportWhenLanguageCodeAlreadySetForDistrict() throws Exception {
        Reader reader = createReaderWithHeaders("L1,Lang 1,Circle 4,State 4,District 41,N");
        languageLocationImportService.importData(reader);
    }

    @Test(expected = CsvImportDataException.class)
    public void testImportWhenDistrictStateDoesNotMatch() throws Exception {
        Reader reader = createReaderWithHeaders("L1,Lang 1,Circle 1,State 1,District 21,N");
        languageLocationImportService.importData(reader);
    }

    @Test(expected = CsvImportDataException.class)
    public void testImportWhenLanguageCodeExistsAndLanguageNameDoesNotMatch() throws Exception {
        Reader reader = createReaderWithHeaders("L2,Lang 3,Circle 3,State 3,District 31,N");
        languageLocationImportService.importData(reader);
    }

    @Test(expected = CsvImportDataException.class)
    public void testImportWhenLanguageCodeNotExistsAndIsDefaultForCircleButNotUnique() throws Exception {
        Reader reader = createReaderWithHeaders("L3,Lang 3,Circle 4,State 4,District 42,Y");
        languageLocationImportService.importData(reader);
    }

    private Reader createReaderWithHeaders(String... lines) {
        StringBuilder builder = new StringBuilder();
        builder.append("languagelocation code,Language,Circle,State,District,Default Language for Circle (Y/N)").append("\n");
        for (String line : lines) {
            builder.append(line).append("\n");
        }
        return new StringReader(builder.toString());
    }

    private void assertLanguageCode(Language language, String code, String languageName) {
        assertNotNull(language);
        assertEquals(code, language.getCode());
        assertNotNull(language.getName());
        assertEquals(languageName, language.getName());
    }
}
