package org.motechproject.nms.testing.it.region;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.motechproject.nms.csv.exception.CsvImportDataException;
import org.motechproject.nms.region.csv.LanguageLocationImportService;
import org.motechproject.nms.region.domain.Circle;
import org.motechproject.nms.region.domain.District;
import org.motechproject.nms.region.domain.Language;
import org.motechproject.nms.region.domain.State;
import org.motechproject.nms.region.repository.CircleDataService;
import org.motechproject.nms.region.repository.DistrictDataService;
import org.motechproject.nms.region.repository.LanguageDataService;
import org.motechproject.nms.region.repository.StateDataService;
import org.motechproject.nms.region.service.CircleService;
import org.motechproject.nms.region.service.DistrictService;
import org.motechproject.nms.region.service.StateService;
import org.motechproject.nms.testing.it.api.utils.RequestBuilder;
import org.motechproject.nms.testing.service.TestingService;
import org.motechproject.testing.osgi.BasePaxIT;
import org.motechproject.testing.osgi.container.MotechNativeTestContainerFactory;
import org.motechproject.testing.osgi.http.SimpleHttpClient;
import org.motechproject.testing.utils.TestContext;
import org.ops4j.pax.exam.ExamFactory;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.motechproject.nms.testing.it.utils.RegionHelper.createCircle;
import static org.motechproject.nms.testing.it.utils.RegionHelper.createDistrict;
import static org.motechproject.nms.testing.it.utils.RegionHelper.createState;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@ExamFactory(MotechNativeTestContainerFactory.class)
public class LanguageLocationCodesImportServiceBundleIT extends BasePaxIT {

    @Inject
    TestingService testingService;
    @Inject
    LanguageDataService languageDataService;
    @Inject
    StateDataService stateDataService;
    @Inject
    StateService stateService;
    @Inject
    DistrictDataService districtDataService;
    @Inject
    DistrictService districtService;
    @Inject
    CircleDataService circleDataService;
    @Inject
    CircleService circleService;
    @Inject
    LanguageLocationImportService languageLocationImportService;


    private State state1;
    private State state3;


    @Before
    public void setUp() {
        testingService.clearDatabase();

        Language lang1 = new Language("L1", "Lang 1");
        languageDataService.create(lang1);

        Language lang2 = new Language("L2", "Lang 2");
        languageDataService.create(lang2);

        Circle circle1 = createCircle("Circle 1");
        circleDataService.create(circle1);

        Circle circle2 = createCircle("Circle 2");
        circleDataService.create(circle2);

        Circle circle3 = createCircle("Circle 3");
        circleDataService.create(circle3);

        Circle circle4 = createCircle("Circle 4");
        circle4.setDefaultLanguage(lang1);
        circleDataService.create(circle4);

        state1 = createState(1L, "State 1");
        District district11 = createDistrict(state1, 11L, "District 11", null, circle1);
        District district12 = createDistrict(state1, 12L, "District 12", null, circle1);
        state1.getDistricts().addAll(Arrays.asList(district11, district12));
        stateDataService.create(state1);

        State state2 = createState(2L, "State 2");
        District district21 = createDistrict(state2, 21L, "District 21", null, circle1);
        state2.getDistricts().addAll(Collections.singletonList(district21));
        districtDataService.create(district21);

        state3 = createState(3L, "State 3");
        District district31 = createDistrict(state3, 31L, "District 31", null, circle2);
        District district32 = createDistrict(state3, 32L, "District 32", lang1, circle3);
        state3.getDistricts().addAll(Arrays.asList(district31, district32));
        stateDataService.create(state3);

        State state4 = createState(4L, "State 4");
        District district41 = createDistrict(state4, 41L, "District 41", lang1, circle4);
        District district42 = createDistrict(state4, 42L, "District 42", null, circle4);
        state4.getDistricts().addAll(Arrays.asList(district41, district42));
        stateDataService.create(state4);
    }

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void testImportWhenStateAndDistrictPresent() throws Exception {
        Reader reader = createReaderWithHeaders("L1,Lang 1,Circle 1,State 1,District 11,N");
        languageLocationImportService.importData(reader);

        District district11 = districtService.findByStateAndCode(state1, 11L);
        assertLanguageCode(district11.getLanguage(), "L1", "Lang 1");
    }

    @Test
    public void testImportWhenOnlyStatePresent() throws Exception {
        Reader reader = createReaderWithHeaders("L1,Lang 1,Circle 1,State 1,,N");
        languageLocationImportService.importData(reader);

        District district11 = districtService.findByStateAndCode(state1, 11L);
        assertLanguageCode(district11.getLanguage(), "L1", "Lang 1");

        District district12 = districtService.findByStateAndCode(state1, 12L);
        assertLanguageCode(district12.getLanguage(), "L1", "Lang 1");
    }

    @Test
    public void testImportWhenOnlyDistrictPresent() throws Exception {
        Reader reader = createReaderWithHeaders("L1,Lang 1,Circle 1,,District 11,N");
        exception.expect(CsvImportDataException.class);
        languageLocationImportService.importData(reader);
    }

    @Test
    public void testImportWhenLanguageCodeExists() throws Exception {
        Reader reader = createReaderWithHeaders("L1,Lang 1,Circle 3,State 3,District 31,N");
        languageLocationImportService.importData(reader);

        District district32 = districtService.findByStateAndCode(state3, 31L);
        assertLanguageCode(district32.getLanguage(), "L1", "Lang 1");
    }

    @Test
    public void testImportWhenStateAndDistrictAreNull() throws Exception {
        Reader reader = createReaderWithHeaders("L1,Lang 1,Circle 1,,,N");
        exception.expect(CsvImportDataException.class);
        languageLocationImportService.importData(reader);
    }

    @Test
    public void testImportWhenLanguageCodeAlreadySetForDistrict() throws Exception {
        Reader reader = createReaderWithHeaders("L1,Lang 1,Circle 4,State 4,District 41,N");
        exception.expect(CsvImportDataException.class);
        languageLocationImportService.importData(reader);
    }

    @Test
    public void testImportWhenDistrictStateDoesNotMatch() throws Exception {
        Reader reader = createReaderWithHeaders("L1,Lang 1,Circle 1,State 1,District 21,N");
        exception.expect(CsvImportDataException.class);
        languageLocationImportService.importData(reader);
    }

    @Test
    public void testImportWhenLanguageCodeExistsAndLanguageNameDoesNotMatch() throws Exception {
        Reader reader = createReaderWithHeaders("L2,Lang 3,Circle 3,State 3,District 31,N");
        exception.expect(CsvImportDataException.class);
        languageLocationImportService.importData(reader);
    }

    @Test
    public void testImportWhenLanguageCodeNotExistsAndIsDefaultForCircleButNotUnique() throws Exception {
        Reader reader = createReaderWithHeaders("L3,Lang 3,Circle 4,State 4,District 42,Y");
        exception.expect(CsvImportDataException.class);
        languageLocationImportService.importData(reader);
    }


    /**
     * To verify language location code is rejected when mandatory parameter circle is missing.
     */
    @Test
    public void verifyFT266() throws Exception {
        Reader reader = createReaderWithHeaders("L1,Lang 1,,State 1,District 11,Y");
        exception.expect(CsvImportDataException.class);
        languageLocationImportService.importData(reader);
    }

    /**
     * To verify language location code is rejected when state is having invalid value.
     */
    @Test
    public void verifyFT269() throws Exception {
        Boolean thrown = false;
        String errorMessage = "State does not exist";
        Reader reader = createReaderWithHeaders("L1,Lang 1,Circle 1,State 10,,N");
        try {
            languageLocationImportService.importData(reader);
        } catch (CsvImportDataException e) {
            thrown = true;
            assertTrue(errorMessage.equals(e.getMessage()));
        }
        assertTrue(thrown);
    }

    /**
     * To verify language location code is rejected when district is having invalid value.
     */
    @Test
    public void verifyFT271() throws Exception {
        Boolean thrown = false;
        String errorMessage = "District District 31 doesn't exist in state State 1";
        Reader reader = createReaderWithHeaders("L1,Lang 1,Circle 1,State 1,District 31,N");
        try {
            languageLocationImportService.importData(reader);
        } catch (CsvImportDataException e) {
            thrown = true;
            assertTrue(errorMessage.equals(e.getMessage()));
        }
        assertTrue(thrown);
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

    /**
     * Method used to import CSV File For Location Data i.e circle, state and
     * district & language location code
     */
    private HttpResponse importCsvFileForLocationData(String location,
            String fileName)
            throws InterruptedException, IOException {
        HttpPost httpPost;
        if (null==location) {
            httpPost = new HttpPost(String.format(
                    "http://localhost:%d/region/languageLocationCode/import",
                    TestContext.getJettyPort()));
        } else {
            httpPost = new HttpPost(String.format(
                    "http://localhost:%d/region/data/import/%s",
                    TestContext.getJettyPort(), location));
        }
        FileBody fileBody = new FileBody(new File(String.format(
                "src/test/resources/csv/%s", fileName)));
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        builder.addPart("csvFile", fileBody);
        httpPost.setEntity(builder.build());
        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(
                httpPost, RequestBuilder.ADMIN_USERNAME,
                RequestBuilder.ADMIN_PASSWORD);
        return response;
    }

    /**
     * To verify that multiple states can be mapped to one circle.
     */
    @Test
    public void verifyFT519() throws InterruptedException, IOException {
        stateDataService.evictAllCache();

        // Circle 1 circle have two states
        State state1 = stateDataService.findByCode(1L);
        State state2 = stateDataService.findByCode(2L);
        Circle circle = circleDataService.findByName("Circle 1");

        assertTrue(stateService.getAllInCircle(circle).size() > 1);
        assertTrue(stateService.getAllInCircle(circle).contains(state1));
        assertTrue(stateService.getAllInCircle(circle).contains(state2));
    }

    /**
     * To verify that one state can be mapped to more than one circle.
     */
    @Test
    public void verifyFT520() throws InterruptedException, IOException {
        stateDataService.evictAllCache();

        State state = stateDataService.findByCode(3L);
        Circle circleA = circleDataService.findByName("Circle 2");
        Circle circleB = circleDataService.findByName("Circle 3");

        // state3 mapped to two circles
        assertTrue(circleService.getAllInState(state).size() > 1);
        assertTrue(circleService.getAllInState(state).contains(circleA));
        assertTrue(circleService.getAllInState(state).contains(circleB));
    }
}
