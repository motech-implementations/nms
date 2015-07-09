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
import org.motechproject.nms.region.domain.State;
import org.motechproject.nms.region.domain.Taluka;
import org.motechproject.nms.region.domain.Village;
import org.motechproject.nms.region.repository.DistrictDataService;
import org.motechproject.nms.region.repository.HealthBlockDataService;
import org.motechproject.nms.region.repository.HealthFacilityDataService;
import org.motechproject.nms.region.repository.HealthFacilityTypeDataService;
import org.motechproject.nms.region.repository.StateDataService;
import org.motechproject.nms.region.repository.TalukaDataService;
import org.motechproject.nms.region.repository.VillageDataService;
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

import static org.junit.Assert.assertEquals;

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

    public static final String SUCCESS = "Success";

    public static final String FAILURE = "Failure: ";

    @Before
    public void setUp() {
        testingService.clearDatabase();
    }

    private State createState() {
        State state = stateDataService.create(new State("Delhi", 1234L));
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
        district = districtDataService.create(district);
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
    private void importCsvFileForLocationData(String location, String fileName)
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
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
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
        importCsvFileForLocationData("state", "state.csv");

        State updatedState = stateDataService.findByCode(1234l);
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
        originalDistrict = districtDataService.create(originalDistrict);
        assertEquals("bihar", originalDistrict.getName());
        assertEquals("bihar region", originalDistrict.getRegionalName());

        // update district name to "district name" using district.csv
        importCsvFileForLocationData("district", "district.csv");

        District updatedDistrict = districtDataService.retrieve("code", 1l);
        assertEquals(originalDistrict.getId(), updatedDistrict.getId());// refer
                                                                        // same
                                                                        // district
        assertEquals("district name", updatedDistrict.getName());
        assertEquals("district regional name",
                updatedDistrict.getRegionalName());
        // TODO Audit trail log assert
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

        // update healthBlock using health_block.csv
        importCsvFileForLocationData("healthBlock", "health_block.csv");

        HealthBlock updatedHealthBlock = healthBlockDataService.retrieve(
                "code", 6l);
        assertEquals(orignalHealthBlock.getId(), updatedHealthBlock.getId());
        assertEquals("health block name", updatedHealthBlock.getName());
        assertEquals("health block regional name",
                updatedHealthBlock.getRegionalName());
        assertEquals("health block hq", updatedHealthBlock.getHq());
        // TODO Audit trail log assert
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
        importCsvFileForLocationData("healthFacility", "health_facility.csv");

        HealthFacility updatedHealthFacility = healthFacilityDataService
                .retrieve("code", 7l);
        assertEquals(originalHealthFacility.getId(),
                updatedHealthFacility.getId());
        assertEquals("health facility name", updatedHealthFacility.getName());
        assertEquals("health facility regional name",
                updatedHealthFacility.getRegionalName());
        // TODO Audit trail log assert
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

        // update census village using census_village.csv
        importCsvFileForLocationData("censusVillage", "census_village.csv");

        Village updatedCensusVillage = villageDataService.retrieve("code", 3l);
        assertEquals(originalCensusVillage.getId(),
                updatedCensusVillage.getId());
        assertEquals("census village name", updatedCensusVillage.getName());
        assertEquals("census village regional name",
                updatedCensusVillage.getRegionalName());
        // TODO Audit trail log assert
    }

}
