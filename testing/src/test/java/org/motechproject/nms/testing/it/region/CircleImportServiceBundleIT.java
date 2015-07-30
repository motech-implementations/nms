package org.motechproject.nms.testing.it.region;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.nms.csv.domain.CsvAuditRecord;
import org.motechproject.nms.csv.exception.CsvImportDataException;
import org.motechproject.nms.csv.repository.CsvAuditRecordDataService;
import org.motechproject.nms.region.csv.CircleImportService;
import org.motechproject.nms.region.domain.Circle;
import org.motechproject.nms.region.domain.State;
import org.motechproject.nms.region.repository.CircleDataService;
import org.motechproject.nms.region.repository.StateDataService;
import org.motechproject.nms.testing.it.api.utils.RequestBuilder;
import org.motechproject.nms.testing.service.TestingService;
import org.motechproject.nms.tracking.domain.ChangeLog;
import org.motechproject.nms.tracking.repository.ChangeLogDataService;
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
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;


@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@ExamFactory(MotechNativeTestContainerFactory.class)
public class CircleImportServiceBundleIT extends BasePaxIT {
    @Inject
    TestingService testingService;
    @Inject
    CircleDataService circleDataService;
    @Inject
    StateDataService stateDataService;
    @Inject
    CircleImportService circleImportService;

    private String headers = "Circle,State";

    private State Gujarat;
    private State Daman;
    private State Dadra;
    private State Haryana;
    private State Pradesh;
    private State Uttarakhand;

    @Inject
    private CsvAuditRecordDataService csvAuditRecordDataService;

    public static final String SUCCESS = "Success";

    public static final String FAILURE = "Failure: ";

    @Inject
    private ChangeLogDataService changeLogDataService;

    @Before
    public void setUp() {

        testingService.clearDatabase();

        Gujarat = stateDataService.create(new State("Gujarat", 1L));
        Daman = stateDataService.create(new State("Daman & Diu", 2L));
        Dadra = stateDataService.create(new State("Dadra & Nagar Haveli", 3L));
        Haryana = stateDataService.create(new State("Haryana", 4L));
        Pradesh = stateDataService.create(new State("Uttar Pradesh", 5L));
        Uttarakhand = stateDataService.create(new State("Uttarakhand", 6L));
    }

    // Test missing circle name
    @Test(expected = CsvImportDataException.class)
    public void testMissingCircleName() throws Exception {
        Reader reader = createReaderWithHeaders(headers, ",Haryana");
        circleImportService.importData(reader);
    }

    // Test missing state
    @Test(expected = CsvImportDataException.class)
    public void testMissingStateName() throws Exception {
        Reader reader = createReaderWithHeaders(headers, "Haryana,");
        circleImportService.importData(reader);
    }

    // Test no state for code in csv file
    @Test(expected = CsvImportDataException.class)
    public void testNoStateForName() throws Exception {
        Reader reader = createReaderWithHeaders(headers, "Haryana,BlahBlah");
        circleImportService.importData(reader);
    }

    // Test file import
    @Test
    public void testFileImport() throws Exception {
        State state;
        circleImportService.importData(read("csv/circles.csv"));

        // What to assert?
        Circle circle = circleDataService.findByName("Gujarat & Daman & Diu");
        assertNotNull(circle);
        assertEquals(3, circle.getStates().size());
        assertTrue(circle.getStates().contains(Gujarat));
        assertTrue(circle.getStates().contains(Daman));
        assertTrue(circle.getStates().contains(Dadra));

        state = stateDataService.findByName("Gujarat");
        assertNotNull(state);
        assertEquals(1, state.getCircles().size());
        assertTrue(state.getCircles().contains(circle));

        state = stateDataService.findByName("Daman & Diu");
        assertNotNull(state);
        assertEquals(1, state.getCircles().size());
        assertTrue(state.getCircles().contains(circle));

        state = stateDataService.findByName("Dadra & Nagar Haveli");
        assertNotNull(state);
        assertEquals(1, state.getCircles().size());
        assertTrue(state.getCircles().contains(circle));

        circle = circleDataService.findByName("Haryana");
        assertNotNull(circle);
        assertEquals(1, circle.getStates().size());
        assertTrue(circle.getStates().contains(Haryana));

        state = stateDataService.findByName("Haryana");
        assertNotNull(state);
        assertEquals(1, state.getCircles().size());
        assertTrue(state.getCircles().contains(circle));

        circle = circleDataService.findByName("Uttar Pradesh(East)");
        assertNotNull(circle);
        assertEquals(1, circle.getStates().size());
        assertTrue(circle.getStates().contains(Pradesh));

        state = stateDataService.findByName("Uttar Pradesh");
        assertNotNull(state);
        assertEquals(2, state.getCircles().size());
        assertTrue(state.getCircles().contains(circle));

        circle = circleDataService.findByName("Uttar Pradesh(West) & Uttarakhand");
        assertNotNull(circle);
        assertEquals(2, circle.getStates().size());
        assertTrue(circle.getStates().contains(Pradesh));
        assertTrue(circle.getStates().contains(Uttarakhand));

        state = stateDataService.findByName("Uttar Pradesh");
        assertNotNull(state);
        assertEquals(2, state.getCircles().size());
        assertTrue(state.getCircles().contains(circle));

        state = stateDataService.findByName("Uttarakhand");
        assertNotNull(state);
        assertEquals(1, state.getCircles().size());
        assertTrue(state.getCircles().contains(circle));
    }

    private Reader createReaderWithHeaders(String header, String... lines) {
        StringBuilder builder = new StringBuilder();
        builder.append(header);
        builder.append("\r\n");

        for (String line : lines) {
            builder.append(line).append("\r\n");
        }
        return new StringReader(builder.toString());
    }

    private Reader read(String resource) {
        return new InputStreamReader(getClass().getClassLoader().getResourceAsStream(resource));
    }

    /**
     * Method used to import CSV File For Location Data
     */
    private HttpResponse importCsvFileForLocationData(String location,
            String fileName)
            throws InterruptedException, IOException {
        HttpPost httpPost = new HttpPost(String.format(
                "http://localhost:%d/region/data/import/%s",
                TestContext.getJettyPort(), location));
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
     * To verify circle location data is uploaded successfully.
     */
    // TODO https://applab.atlassian.net/browse/NMS-242
    @Test
    public void verifyFT262() throws InterruptedException, IOException {
        changeLogDataService.deleteAll();
        HttpResponse response = importCsvFileForLocationData("circle",
                "circles.csv");
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        // assert circle data
        Circle circle = circleDataService.findByName("Gujarat & Daman & Diu");
        assertNotNull(circle);
        assertEquals(3, circle.getStates().size());
        assertTrue(circle.getStates().contains(Gujarat));
        assertTrue(circle.getStates().contains(Daman));
        assertTrue(circle.getStates().contains(Dadra));

        assertNotNull(circleDataService.findByName("Uttar Pradesh(East)"));
        assertNotNull(circleDataService
                .findByName("Uttar Pradesh(West) & Uttarakhand"));
        // Assert audit trail log
        CsvAuditRecord csvAuditRecord = csvAuditRecordDataService.retrieveAll()
                .get(0);
        assertEquals("region/data/import/circle", csvAuditRecord.getEndpoint());
        assertEquals(SUCCESS, csvAuditRecord.getOutcome());
        assertEquals("circles.csv", csvAuditRecord.getFile());

        // assert location history
        boolean locationHistoryAssert = false;
        List<ChangeLog> changeLogs = changeLogDataService
                .findByEntityNameAndInstanceId(circle.getClass().getName(),
                        circle.getId());
        for (ChangeLog changeLog : changeLogs) {
            if (changeLog.getChange().contains(
                    "name(null, Gujarat & Daman & Diu)")) {
                locationHistoryAssert = true;
                break;
            }
        }

        assertTrue(locationHistoryAssert);
    }

    /**
     * To verify circle location data is rejected when mandatory parameter
     * circle name is missing.
     */
    @Test
    public void verifyFT264() throws InterruptedException, IOException {
        changeLogDataService.deleteAll();
        HttpResponse response = importCsvFileForLocationData("circle",
                "circle_ft_264.csv");
        assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatusLine()
                .getStatusCode());
        // assert circle data
        assertNull(circleDataService.findByName("Gujarat & Daman & Diu"));
        assertNull(circleDataService.findByName("Uttar Pradesh(East)"));
        assertNull(circleDataService
                .findByName("Uttar Pradesh(West) & Uttarakhand"));

        // Assert audit trail log
        CsvAuditRecord csvAuditRecord = csvAuditRecordDataService.retrieveAll()
                .get(0);
        assertEquals("region/data/import/circle", csvAuditRecord.getEndpoint());
        assertTrue(csvAuditRecord.getOutcome().contains(FAILURE));
        assertEquals("circle_ft_264.csv", csvAuditRecord.getFile());
    }
}