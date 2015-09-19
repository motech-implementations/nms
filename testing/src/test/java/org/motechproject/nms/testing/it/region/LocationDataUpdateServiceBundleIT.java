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
import org.motechproject.nms.csv.repository.CsvAuditRecordDataService;
import org.motechproject.nms.region.domain.District;
import org.motechproject.nms.region.domain.HealthBlock;
import org.motechproject.nms.region.domain.HealthFacility;
import org.motechproject.nms.region.domain.HealthFacilityType;
import org.motechproject.nms.region.domain.HealthSubFacility;
import org.motechproject.nms.region.domain.State;
import org.motechproject.nms.region.domain.Taluka;
import org.motechproject.nms.region.domain.Village;
import org.motechproject.nms.region.repository.DistrictDataService;
import org.motechproject.nms.region.repository.HealthBlockDataService;
import org.motechproject.nms.region.repository.HealthFacilityDataService;
import org.motechproject.nms.region.repository.HealthFacilityTypeDataService;
import org.motechproject.nms.region.repository.HealthSubFacilityDataService;
import org.motechproject.nms.region.repository.StateDataService;
import org.motechproject.nms.region.repository.TalukaDataService;
import org.motechproject.nms.region.repository.VillageDataService;
import org.motechproject.nms.region.service.DistrictService;
import org.motechproject.nms.region.service.StateService;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Integration tests for Location Data Import Controller
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@ExamFactory(MotechNativeTestContainerFactory.class)
public class LocationDataUpdateServiceBundleIT extends BasePaxIT {

    @Inject
    private TestingService testingService;

    @Inject
    private StateDataService stateDataService;

    @Inject
    private StateService stateService;

    @Inject
    private DistrictService districtService;

    @Inject
    private DistrictDataService districtDataService;

    @Inject
    private TalukaDataService talukaDataService;

    @Inject
    private HealthBlockDataService healthBlockDataService;

    @Inject
    private HealthFacilityTypeDataService healthFacilityTypeDataService;

    @Inject
    private HealthFacilityDataService healthFacilityDataService;

    @Inject
    private VillageDataService villageDataService;

    @Inject
    private CsvAuditRecordDataService csvAuditRecordDataService;

    @Inject
    private ChangeLogDataService changeLogDataService;

    @Inject
    private HealthSubFacilityDataService healthSubFacilityDataService;

    public static final String SUCCESS = "Success";

    public static final String FAILURE = "Failure: ";

    @Before
    public void setUp() {
        testingService.clearDatabase();
        changeLogDataService.deleteAll();
    }

    private State createState() {
        State state = stateService.create(new State("Delhi", 1234L));
        return state;
    }

    private Taluka createTaluka(District district) {
        Taluka taluka = new Taluka();
        taluka.setDistrict(district);
        taluka.setCode("TALUKA");
        taluka.setName("taluka name");
        taluka.setRegionalName("taluka regional name");
        taluka.setIdentity(2);
        talukaDataService.create(taluka);
        return taluka;
    }

    private District createDistrict(State state) {
        District district = new District();
        district.setCode(1l);
        district.setName("district name");
        district.setState(state);
        district.setRegionalName("district regional name");
        district = districtService.create(district);
        return district;
    }

    private HealthBlock createHealthBlock(Taluka taluka) {
        HealthBlock healthBlock = new HealthBlock();
        healthBlock.setTaluka(taluka);
        healthBlock.setCode(6l);
        healthBlock.setName("health block name");
        healthBlock.setRegionalName("health block regional name");
        healthBlock.setHq("health block hq");
        healthBlock = healthBlockDataService.create(healthBlock);
        return healthBlock;
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
        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(httpPost,
                                                  RequestBuilder.ADMIN_USERNAME, RequestBuilder.ADMIN_PASSWORD);
        return response;
    }

    /**
     * To verify state location data is updated successfully.
     */
    // TODO https://applab.atlassian.net/browse/NMS-229
    @Test
    public void verifyFT218() throws InterruptedException, IOException {
        // add state with name as "Haryana"
        State orginalState = stateDataService
                .create(new State("Haryana", 1234L));
        assertEquals("Haryana", orginalState.getName());

        // update state name to "Delhi" using state.csv
        HttpResponse response = importCsvFileForLocationData("state",
                "state.csv");
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());

        State updatedState = stateService.findByCode(1234l);
        assertEquals(orginalState.getId(), updatedState.getId());// refer same
                                                                 // state
        assertEquals("Delhi", updatedState.getName());

        //Assert audit trail log
        CsvAuditRecord csvAuditRecord=csvAuditRecordDataService.retrieveAll().get(0);
        assertEquals("region/data/import/state", csvAuditRecord.getEndpoint());
        assertEquals(SUCCESS, csvAuditRecord.getOutcome());
        assertEquals("state.csv", csvAuditRecord.getFile());
    }

    /**
     * To verify district location data is updated successfully.
     */
    @Test
    public void verifyFT225() throws InterruptedException, IOException {
        // add state
        State state = createState();

        // add district with name as "bihar" and code as 1l
        District originalDistrict = new District();
        originalDistrict.setCode(1l);
        originalDistrict.setName("bihar");
        originalDistrict.setState(state);
        originalDistrict.setRegionalName("bihar region");
        originalDistrict = districtService.create(originalDistrict);
        assertEquals("bihar", originalDistrict.getName());
        assertEquals("bihar region", originalDistrict.getRegionalName());
        
        ChangeLog changeLog=changeLogDataService.findByEntityNameAndInstanceId(originalDistrict.getClass().getName(), originalDistrict.getId()).get(0);
        assertTrue(changeLog.getChange().contains("regionalName(null, bihar region)"));
        assertTrue(changeLog.getChange().contains("name(null, bihar)"));
        changeLogDataService.delete(changeLog);

        // update district name to "district name" using district.csv
        HttpResponse response = importCsvFileForLocationData("district",
                "district.csv");
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());

        District updatedDistrict = districtDataService.retrieve("code", 1l);
        assertEquals(originalDistrict.getId(), updatedDistrict.getId());// refer
                                                                        // same
                                                                        // district
        assertEquals("district name", updatedDistrict.getName());
        assertEquals("district regional name",
                updatedDistrict.getRegionalName());

        // Assert audit trail log
        CsvAuditRecord csvAuditRecord = csvAuditRecordDataService.retrieveAll()
                .get(0);
        assertEquals("region/data/import/district",
                csvAuditRecord.getEndpoint());
        assertEquals(SUCCESS, csvAuditRecord.getOutcome());
        assertEquals("district.csv", csvAuditRecord.getFile());
        
        // assert location history
        changeLog = changeLogDataService
                .findByEntityNameAndInstanceId(
                        originalDistrict.getClass().getName(),
                        originalDistrict.getId()).get(0);
        assertTrue(changeLog.getChange().contains(
                "regionalName(bihar region, district regional name)"));
        assertTrue(changeLog.getChange().contains("name(bihar, district name)"));
    }

    /**
     * To verify health block location data is updated successfully.
     */
    @Test
    public void verifyFT239() throws InterruptedException, IOException {
        // add state
        State state = createState();

        // add district
        District district = createDistrict(state);

        // add taluka
        Taluka taluka = createTaluka(district);

        // add health block
        HealthBlock orignalHealthBlock = new HealthBlock();
        orignalHealthBlock.setTaluka(taluka);
        orignalHealthBlock.setCode(6l);
        orignalHealthBlock.setName("name");
        orignalHealthBlock.setRegionalName("rn");
        orignalHealthBlock.setHq("hq");
        orignalHealthBlock = healthBlockDataService.create(orignalHealthBlock);
        assertEquals("name", orignalHealthBlock.getName());

        ChangeLog changeLog = changeLogDataService
                .findByEntityNameAndInstanceId(
                        orignalHealthBlock.getClass().getName(),
                        orignalHealthBlock.getId()).get(0);
        assertTrue(changeLog.getChange().contains("regionalName(null, rn)"));
        assertTrue(changeLog.getChange().contains("name(null, name)"));
        assertTrue(changeLog.getChange().contains("hq(null, hq)"));
        changeLogDataService.delete(changeLog);

        // update healthBlock using health_block.csv
        HttpResponse response = importCsvFileForLocationData("healthBlock",
                "health_block.csv");
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());

        HealthBlock updatedHealthBlock = healthBlockDataService.retrieve(
                "code", 6l);
        assertEquals(orignalHealthBlock.getId(), updatedHealthBlock.getId());
        assertEquals("health block name", updatedHealthBlock.getName());
        assertEquals("health block regional name",
                updatedHealthBlock.getRegionalName());
        assertEquals("health block hq", updatedHealthBlock.getHq());

        // Assert audit trail log
        CsvAuditRecord csvAuditRecord = csvAuditRecordDataService.retrieveAll()
                .get(0);
        assertEquals("region/data/import/healthBlock",
                csvAuditRecord.getEndpoint());
        assertEquals(SUCCESS, csvAuditRecord.getOutcome());
        assertEquals("health_block.csv", csvAuditRecord.getFile());

        // assert location history
        changeLog = changeLogDataService.findByEntityNameAndInstanceId(
                orignalHealthBlock.getClass().getName(),
                orignalHealthBlock.getId()).get(0);
        assertTrue(changeLog.getChange().contains(
                "regionalName(rn, health block regional name)"));
        assertTrue(changeLog.getChange().contains(
                "name(name, health block name)"));
        assertTrue(changeLog.getChange().contains("hq(hq, health block hq)"));
    }

    /**
     * To verify health facility location data is updated successfully.
     */
    @Test
    public void verifyFT246() throws InterruptedException, IOException {
        // add state
        State state = createState();

        // add district
        District district = createDistrict(state);

        // add taluka
        Taluka taluka = createTaluka(district);

        // add health block
        HealthBlock healthBlock = createHealthBlock(taluka);

        // add health facility type
        HealthFacilityType healthFacilityType = new HealthFacilityType();
        healthFacilityType.setName("type");
        healthFacilityType.setCode(5678l);
        healthFacilityType = healthFacilityTypeDataService
                .create(healthFacilityType);

        // add health facility
        HealthFacility originalHealthFacility = new HealthFacility();
        originalHealthFacility.setHealthBlock(healthBlock);
        originalHealthFacility.setCode(7l);
        originalHealthFacility.setName("name");
        originalHealthFacility.setRegionalName("regional name");
        originalHealthFacility.setHealthFacilityType(healthFacilityType);
        originalHealthFacility = healthFacilityDataService
                .create(originalHealthFacility);
        assertEquals("name", originalHealthFacility.getName());

        // update health facility using health_facility.csv
        HttpResponse response = importCsvFileForLocationData("healthFacility",
                "health_facility.csv");
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());

        HealthFacility updatedHealthFacility = healthFacilityDataService
                .retrieve("code", 7l);
        assertEquals(originalHealthFacility.getId(),
                updatedHealthFacility.getId());
        assertEquals("health facility name", updatedHealthFacility.getName());
        assertEquals("health facility regional name",
                updatedHealthFacility.getRegionalName());

        CsvAuditRecord csvAuditRecord = csvAuditRecordDataService.retrieveAll()
                .get(0);
        assertEquals("region/data/import/healthFacility",
                csvAuditRecord.getEndpoint());
        assertEquals(SUCCESS, csvAuditRecord.getOutcome());
        assertEquals("health_facility.csv", csvAuditRecord.getFile());
    }

    /**
     * To verify village location data is updated successfully.
     */
    @Test
    public void verifyFT260() throws InterruptedException, IOException {
        // add state
        State state = createState();

        // add district
        District district = createDistrict(state);

        // add taluka
        Taluka taluka = createTaluka(district);

        // add Census village
        Village originalCensusVillage = new Village();
        originalCensusVillage.setName("name");
        originalCensusVillage.setRegionalName("rn");
        originalCensusVillage.setTaluka(taluka);
        originalCensusVillage.setVcode(3l);
        originalCensusVillage = villageDataService
                .create(originalCensusVillage);
        assertEquals("name", originalCensusVillage.getName());

        ChangeLog changeLog = changeLogDataService
                .findByEntityNameAndInstanceId(
                        originalCensusVillage.getClass().getName(),
                        originalCensusVillage.getId()).get(0);
        assertTrue(changeLog.getChange().contains(
                "regionalName(null, rn)"));
        assertTrue(changeLog.getChange().contains("name(null, name)"));
        changeLogDataService.delete(changeLog);

        // update census village using census_village.csv
        HttpResponse response = importCsvFileForLocationData("censusVillage",
                "census_village.csv");
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());

        Village updatedCensusVillage = villageDataService.retrieve("code", 3l);
        assertEquals(originalCensusVillage.getId(),
                updatedCensusVillage.getId());
        assertEquals("census village name", updatedCensusVillage.getName());
        assertEquals("census village regional name",
                updatedCensusVillage.getRegionalName());

        // Assert audit trail log
        CsvAuditRecord csvAuditRecord = csvAuditRecordDataService.retrieveAll()
                .get(0);
        assertEquals("region/data/import/censusVillage",
                csvAuditRecord.getEndpoint());
        assertEquals(SUCCESS, csvAuditRecord.getOutcome());
        assertEquals("census_village.csv", csvAuditRecord.getFile());

        // assert location history
        changeLog = changeLogDataService
                .findByEntityNameAndInstanceId(
                        originalCensusVillage.getClass().getName(),
                        originalCensusVillage.getId()).get(0);
        assertTrue(changeLog.getChange().contains(
                "regionalName(rn, census village regional name)"));
        assertTrue(changeLog.getChange().contains(
                "name(name, census village name)"));
    }

    /*
     * To verify taluka location data is rejected when district code is having
     * invalid value.
     */
    @Test
    public void verifyFT231() throws InterruptedException, IOException {
        // add state
        createState();

        // district with district code1 not added

        // add taluka with district code 1 using taluka.csv
        HttpResponse response = importCsvFileForLocationData("taluka",
                "taluka.csv");
        assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatusLine()
                .getStatusCode());

        Taluka taluka = talukaDataService.retrieve("code", "TALUKA");
        assertNull(taluka);

        // Assert audit trail log
        CsvAuditRecord csvAuditRecord = csvAuditRecordDataService.retrieveAll()
                .get(0);
        assertEquals("region/data/import/taluka", csvAuditRecord.getEndpoint());
        assertTrue(csvAuditRecord.getOutcome().contains(FAILURE));
        assertEquals("taluka.csv", csvAuditRecord.getFile());

    }

    /**
     * To verify taluka location data is updated successfully.
     */
    @Test
    public void verifyFT232() throws InterruptedException, IOException {
        // add state
        State state = createState();

        // add district
        District district = createDistrict(state);

        // add taluka
        Taluka originalTaluka = new Taluka();
        originalTaluka.setDistrict(district);
        originalTaluka.setCode("TALUKA");
        originalTaluka.setName("name");
        originalTaluka.setRegionalName("rn");
        originalTaluka.setIdentity(2);
        originalTaluka = talukaDataService.create(originalTaluka);

        assertEquals("name", originalTaluka.getName());
        assertEquals("rn", originalTaluka.getRegionalName());

        ChangeLog changeLog = changeLogDataService
                .findByEntityNameAndInstanceId(
                        originalTaluka.getClass().getName(),
                        originalTaluka.getId()).get(0);
        assertTrue(changeLog.getChange().contains("regionalName(null, rn)"));
        assertTrue(changeLog.getChange().contains("name(null, name)"));
        changeLogDataService.delete(changeLog);

        // update taluka name to "taluka name" using taluka.csv
        HttpResponse response = importCsvFileForLocationData("taluka",
                "taluka.csv");
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());

        Taluka updatedTaluka = talukaDataService.retrieve("code", "TALUKA");
        assertEquals(originalTaluka.getId(), updatedTaluka.getId());// refer
                                                                    // same
                                                                    // taluka
        assertEquals("taluka name", updatedTaluka.getName());
        assertEquals("taluka regional name", updatedTaluka.getRegionalName());

        // Assert audit trail log
        CsvAuditRecord csvAuditRecord = csvAuditRecordDataService.retrieveAll()
                .get(0);
        assertEquals("region/data/import/taluka",
                csvAuditRecord.getEndpoint());
        assertEquals(SUCCESS, csvAuditRecord.getOutcome());
        assertEquals("taluka.csv", csvAuditRecord.getFile());

        // assert location history
        changeLog = changeLogDataService
                .findByEntityNameAndInstanceId(
                originalTaluka.getClass().getName(), originalTaluka.getId())
                .get(0);
        assertTrue(changeLog.getChange().contains(
                "regionalName(rn, taluka regional name)"));
        assertTrue(changeLog.getChange().contains("name(name, taluka name)"));

    }

    /**
     * To verify health sub facility location data is updated successfully.
     */
    @Test
    public void verifyFT253() throws InterruptedException, IOException {
        // add state
        State state = createState();

        // add district
        District district = createDistrict(state);

        // add taluka
        Taluka taluka = createTaluka(district);

        // add health block
        HealthBlock healthBlock = createHealthBlock(taluka);

        // add health facility type
        HealthFacilityType healthFacilityType = new HealthFacilityType();
        healthFacilityType.setName("type");
        healthFacilityType.setCode(5678l);
        healthFacilityType = healthFacilityTypeDataService
                .create(healthFacilityType);

        // add health facility
        HealthFacility healthFacility = new HealthFacility();
        healthFacility.setHealthBlock(healthBlock);
        healthFacility.setCode(7l);
        healthFacility.setName("health facility name");
        healthFacility.setRegionalName("health facility regional name");
        healthFacility.setHealthFacilityType(healthFacilityType);
        healthFacility = healthFacilityDataService
                .create(healthFacility);

        // add health sub facility
        HealthSubFacility orgHealthSubFacility = new HealthSubFacility();
        orgHealthSubFacility.setHealthFacility(healthFacility);
        orgHealthSubFacility.setCode(8l);
        orgHealthSubFacility.setName("name");
        orgHealthSubFacility.setRegionalName("rn");
        orgHealthSubFacility = healthSubFacilityDataService
                .create(orgHealthSubFacility);

        assertEquals("name", orgHealthSubFacility.getName());
        assertEquals("rn", orgHealthSubFacility.getRegionalName());

        ChangeLog changeLog = changeLogDataService
                .findByEntityNameAndInstanceId(
                        orgHealthSubFacility.getClass().getName(),
                        orgHealthSubFacility.getId()).get(0);
        assertTrue(changeLog.getChange().contains("regionalName(null, rn)"));
        assertTrue(changeLog.getChange().contains("name(null, name)"));
        changeLogDataService.delete(changeLog);

        // update health sub facility using health_sub_facility.csv
        HttpResponse response = importCsvFileForLocationData(
                "healthSubFacility", "health_sub_facility.csv");
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());

        HealthSubFacility updatedHealthSubFacility = healthSubFacilityDataService
                .retrieve("code", 8l);
        assertEquals(orgHealthSubFacility.getId(),
                updatedHealthSubFacility.getId());
        assertEquals("health sub facility name",
                updatedHealthSubFacility.getName());
        assertEquals("health sub facility regional name",
                updatedHealthSubFacility.getRegionalName());

        CsvAuditRecord csvAuditRecord = csvAuditRecordDataService.retrieveAll()
                .get(0);
        assertEquals("region/data/import/healthSubFacility",
                csvAuditRecord.getEndpoint());
        assertEquals(SUCCESS, csvAuditRecord.getOutcome());
        assertEquals("health_sub_facility.csv", csvAuditRecord.getFile());

        // assert location history
        changeLog = changeLogDataService.findByEntityNameAndInstanceId(
                orgHealthSubFacility.getClass().getName(),
                orgHealthSubFacility.getId())
                .get(0);
        assertTrue(changeLog.getChange().contains(
                "regionalName(rn, health sub facility regional name)"));
        assertTrue(changeLog.getChange().contains(
                "name(name, health sub facility name)"));
    }

}
