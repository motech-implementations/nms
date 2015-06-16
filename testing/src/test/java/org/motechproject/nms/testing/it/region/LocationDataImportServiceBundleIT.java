package org.motechproject.nms.testing.it.region;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.nms.csv.exception.CsvImportDataException;
import org.motechproject.nms.region.domain.District;
import org.motechproject.nms.region.domain.HealthBlock;
import org.motechproject.nms.region.domain.HealthFacility;
import org.motechproject.nms.region.domain.HealthFacilityType;
import org.motechproject.nms.region.domain.HealthSubFacility;
import org.motechproject.nms.region.domain.State;
import org.motechproject.nms.region.domain.Taluka;
import org.motechproject.nms.region.domain.Village;
import org.motechproject.nms.region.repository.HealthFacilityTypeDataService;
import org.motechproject.nms.region.repository.StateDataService;
import org.motechproject.nms.region.service.CensusVillageImportService;
import org.motechproject.nms.region.service.DistrictImportService;
import org.motechproject.nms.region.service.DistrictService;
import org.motechproject.nms.region.service.HealthBlockImportService;
import org.motechproject.nms.region.service.HealthBlockService;
import org.motechproject.nms.region.service.HealthFacilityImportService;
import org.motechproject.nms.region.service.HealthFacilityService;
import org.motechproject.nms.region.service.HealthSubFacilityImportService;
import org.motechproject.nms.region.service.HealthSubFacilityService;
import org.motechproject.nms.region.service.NonCensusVillageImportService;
import org.motechproject.nms.region.service.TalukaImportService;
import org.motechproject.nms.region.service.TalukaService;
import org.motechproject.nms.region.service.VillageService;
import org.motechproject.nms.testing.service.TestingService;
import org.motechproject.testing.osgi.BasePaxIT;
import org.motechproject.testing.osgi.container.MotechNativeTestContainerFactory;
import org.ops4j.pax.exam.ExamFactory;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;
import org.supercsv.exception.SuperCsvException;

import javax.inject.Inject;
import java.io.InputStreamReader;
import java.io.Reader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@ExamFactory(MotechNativeTestContainerFactory.class)
public class LocationDataImportServiceBundleIT extends BasePaxIT {

    @Inject
    TestingService testingService;

    @Inject
    StateDataService stateDataService;
    @Inject
    DistrictService districtService;
    @Inject
    TalukaService talukaService;
    @Inject
    VillageService villageService;
    @Inject
    HealthBlockService healthBlockService;
    @Inject
    HealthFacilityTypeDataService healthFacilityTypeDataService;
    @Inject
    HealthFacilityService healthFacilityService;
    @Inject
    HealthSubFacilityService healthSubFacilityService;
    @Inject
    DistrictImportService districtImportService;
    @Inject
    TalukaImportService talukaImportService;
    @Inject
    NonCensusVillageImportService nonCensusVillageImportService;
    @Inject
    CensusVillageImportService censusVillageImportService;
    @Inject
    HealthBlockImportService healthBlockImportService;
    @Inject
    HealthFacilityImportService healthFacilityImportService;
    @Inject
    HealthSubFacilityImportService healthSubFacilityImportService;

    
    State exampleState;
    HealthFacilityType exampleFacilityType;

    
    @Before
    public void setUp() {

        testingService.clearDatabase();

        exampleState = stateDataService.create(new State("EXAMPLE STATE", 1234L));
        HealthFacilityType facilityType = new HealthFacilityType();
        facilityType.setName("EXAMPLE FACILITY TYPE");
        facilityType.setCode(5678L);
        exampleFacilityType = healthFacilityTypeDataService.create(facilityType);
    }

    
    @Test
    public void testLocationDataImport() throws Exception {
        districtImportService.importData(read("csv/district.csv"));
        District district = districtService.findByStateAndCode(exampleState, 1L);
        assertNotNull(district);
        assertEquals(1L, (long) district.getCode());
        assertEquals("district name", district.getName());
        assertEquals("district regional name", district.getRegionalName());
        assertNotNull(district.getState());

        talukaImportService.addParent(exampleState);
        talukaImportService.importData(read("csv/taluka.csv"));
        Taluka taluka = talukaService.findByDistrictAndCode(district, "TALUKA");
        assertNotNull(taluka);
        assertEquals("TALUKA", taluka.getCode());
        assertEquals(2, (int) taluka.getIdentity());
        assertEquals("taluka name", taluka.getName());
        assertEquals("taluka regional name", taluka.getRegionalName());
        assertNotNull(taluka.getDistrict());

        censusVillageImportService.addParent(district);
        censusVillageImportService.importData(read("csv/census_village.csv"));
        Village censusVillage = villageService.findByTalukaAndVcodeAndSvid(taluka, 3L, 0);
        assertNotNull(censusVillage);
        assertEquals(3L, (long) censusVillage.getVcode());
        assertEquals("census village name", censusVillage.getName());
        assertEquals("census village regional name", censusVillage.getRegionalName());
        assertNotNull(censusVillage.getTaluka());

        nonCensusVillageImportService.addParent(district);
        nonCensusVillageImportService.importData(read("csv/non_census_village_associated.csv"));
        Village nonCensusVillageAssociated = villageService.findByTalukaAndVcodeAndSvid(taluka, 3L, 4L);
        assertNotNull(nonCensusVillageAssociated);
        assertEquals(4L, nonCensusVillageAssociated.getSvid());
        assertEquals("non census village associated name", nonCensusVillageAssociated.getName());
        assertEquals("non census village associated regional name", nonCensusVillageAssociated.getRegionalName());
        assertNotNull(nonCensusVillageAssociated.getTaluka());
        assertEquals(3L, nonCensusVillageAssociated.getVcode());

        nonCensusVillageImportService.importData(read("csv/non_census_village_non_associated.csv"));
        Village nonCensusVillageNonAssociated = villageService.findByTalukaAndVcodeAndSvid(taluka, 0, 5L);
        assertNotNull(nonCensusVillageNonAssociated);
        assertEquals(5L, nonCensusVillageNonAssociated.getSvid());
        assertEquals("non census village non associated name", nonCensusVillageNonAssociated.getName());
        assertEquals("non census village non associated regional name", nonCensusVillageNonAssociated.getRegionalName());
        assertNotNull(nonCensusVillageNonAssociated.getTaluka());
        assertEquals(0, nonCensusVillageNonAssociated.getVcode());

        healthBlockImportService.addParent(district);
        healthBlockImportService.importData(read("csv/health_block.csv"));
        HealthBlock healthBlock = healthBlockService.findByTalukaAndCode(taluka, 6L);
        assertNotNull(healthBlock);
        assertEquals(6L, (long) healthBlock.getCode());
        assertEquals("health block name", healthBlock.getName());
        assertEquals("health block regional name", healthBlock.getRegionalName());
        assertEquals("health block hq", healthBlock.getHq());
        assertNotNull(healthBlock.getTaluka());

        healthFacilityImportService.addParent(taluka);
        healthFacilityImportService.importData(read("csv/health_facility.csv"));
        HealthFacility healthFacility = healthFacilityService.findByHealthBlockAndCode(healthBlock, 7L);
        assertNotNull(healthFacility);
        assertEquals(7L, (long) healthFacility.getCode());
        assertEquals("health facility name", healthFacility.getName());
        assertEquals("health facility regional name", healthFacility.getRegionalName());
        assertNotNull(healthFacility.getHealthBlock());
        assertNotNull(healthFacility.getHealthFacilityType());

        healthSubFacilityImportService.addParent(healthBlock);
        healthSubFacilityImportService.importData(read("csv/health_sub_facility.csv"));
        HealthSubFacility healthSubFacility = healthSubFacilityService.findByHealthFacilityAndCode(
                healthFacility, 8L);
        assertNotNull(healthSubFacility);
        assertEquals(8L, ((long) healthSubFacility.getCode()));
        assertEquals("health sub facility name", healthSubFacility.getName());
        assertEquals("health sub facility regional name", healthSubFacility.getRegionalName());
        assertNotNull(healthSubFacility.getHealthFacility());
    }

    @Test(expected = SuperCsvException.class)
    public void testThrowExceptionForMalformedCsv() throws Exception {
        districtImportService.importData(read("csv/district_malformed.csv"));
    }

    @Test(expected = CsvImportDataException.class)
    public void testThrowExceptionForInvalidCellFormat() throws Exception {
        districtImportService.importData(read("csv/district_invalid_cell_format.csv"));
    }

    @Test
    public void testRollbackAllAfterSingleFailure() throws Exception {
        boolean thrown = false;
        try {
            districtImportService.importData(read("csv/district_rollback.csv"));
        } catch (Exception e) {
            thrown = true;
        }
        assertTrue(thrown);
        assertNull(districtService.findByStateAndCode(exampleState, 1002L));
        assertNull(districtService.findByStateAndCode(exampleState, 1003L));
        assertNull(districtService.findByStateAndCode(exampleState, 1004L));
    }

    private Reader read(String resource) {
        return new InputStreamReader(getClass().getClassLoader().getResourceAsStream(resource));
    }
}
