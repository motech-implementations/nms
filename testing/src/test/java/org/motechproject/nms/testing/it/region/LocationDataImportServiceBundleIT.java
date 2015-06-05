package org.motechproject.nms.testing.it.region;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.runner.RunWith;
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
import org.motechproject.nms.region.service.CensusVillageImportService;
import org.motechproject.nms.region.service.DistrictImportService;
import org.motechproject.nms.region.service.HealthBlockImportService;
import org.motechproject.nms.region.service.HealthFacilityImportService;
import org.motechproject.nms.region.service.HealthSubFacilityImportService;
import org.motechproject.nms.region.service.NonCensusVillageImportService;
import org.motechproject.nms.region.service.TalukaImportService;
import org.motechproject.nms.testing.service.TestingService;
import org.motechproject.testing.osgi.BasePaxIT;
import org.motechproject.testing.osgi.container.MotechNativeTestContainerFactory;
import org.ops4j.pax.exam.ExamFactory;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;

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
    private TestingService testingService;

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
    private DistrictImportService districtImportService;
    @Inject
    private TalukaImportService talukaImportService;
    @Inject
    private NonCensusVillageImportService nonCensusVillageImportService;
    @Inject
    private CensusVillageImportService censusVillageImportService;
    @Inject
    private HealthBlockImportService healthBlockImportService;
    @Inject
    private HealthFacilityImportService healthFacilityImportService;
    @Inject
    private HealthSubFacilityImportService healthSubFacilityImportService;

    @Before
    public void setUp() {

        testingService.clearDatabase();

        stateDataService.create(new State("EXAMPLE STATE", 1234L));
        HealthFacilityType facilityType = new HealthFacilityType();
        facilityType.setName("EXAMPLE FACILITY TYPE");
        facilityType.setCode(5678L);
        healthFacilityTypeDataService.create(facilityType);
    }

    @Ignore //TEMP
    public void testLocationDataImport() throws Exception {
        districtImportService.importData(read("csv/district.csv"));
        District district = districtDataService.findByCode(1L);
        assertNotNull(district);
        assertEquals(1L, (long) district.getCode());
        assertEquals("district name", district.getName());
        assertEquals("district regional name", district.getRegionalName());
        assertNotNull(district.getState());

        talukaImportService.importData(read("csv/taluka.csv"));
        Taluka taluka = talukaDataService.findByCode("TALUKA");
        assertNotNull(taluka);
        assertEquals("TALUKA", taluka.getCode());
        assertEquals(2, (int) taluka.getIdentity());
        assertEquals("taluka name", taluka.getName());
        assertEquals("taluka regional name", taluka.getRegionalName());
        assertNotNull(taluka.getDistrict());

        censusVillageImportService.importData(read("csv/census_village.csv"));
        Village censusVillage = villageDataService.findByVcodeAndSvid(3L, null);
        assertNotNull(censusVillage);
        assertEquals(3L, (long) censusVillage.getVcode());
        assertEquals("census village name", censusVillage.getName());
        assertEquals("census village regional name", censusVillage.getRegionalName());
        assertNotNull(censusVillage.getTaluka());

        nonCensusVillageImportService.importData(read("csv/non_census_village_associated.csv"));
        Village nonCensusVillageAssociated = villageDataService.findByVcodeAndSvid(3L, 4L);
        assertNotNull(nonCensusVillageAssociated);
        assertEquals(4L, (long) nonCensusVillageAssociated.getSvid());
        assertEquals("non census village associated name", nonCensusVillageAssociated.getName());
        assertEquals("non census village associated regional name", nonCensusVillageAssociated.getRegionalName());
        assertNotNull(nonCensusVillageAssociated.getTaluka());
        assertEquals(3L, (long) nonCensusVillageAssociated.getVcode());

        nonCensusVillageImportService.importData(read("csv/non_census_village_non_associated.csv"));
        Village nonCensusVillageNonAssociated = villageDataService.findByVcodeAndSvid(null, 5L);
        assertNotNull(nonCensusVillageNonAssociated);
        assertEquals(5L, (long) nonCensusVillageNonAssociated.getSvid());
        assertEquals("non census village non associated name", nonCensusVillageNonAssociated.getName());
        assertEquals("non census village non associated regional name", nonCensusVillageNonAssociated.getRegionalName());
        assertNotNull(nonCensusVillageNonAssociated.getTaluka());
        assertNull(nonCensusVillageNonAssociated.getVcode());

        healthBlockImportService.importData(read("csv/health_block.csv"));
        HealthBlock healthBlock = healthBlockDataService.findByCode(6L);
        assertNotNull(healthBlock);
        assertEquals(6L, (long) healthBlock.getCode());
        assertEquals("health block name", healthBlock.getName());
        assertEquals("health block regional name", healthBlock.getRegionalName());
        assertEquals("health block hq", healthBlock.getHq());
        assertNotNull(healthBlock.getTaluka());

        healthFacilityImportService.importData(read("csv/health_facility.csv"));
        HealthFacility healthFacility = healthFacilityDataService.findByCode(7L);
        assertNotNull(healthFacility);
        assertEquals(7L, (long) healthFacility.getCode());
        assertEquals("health facility name", healthFacility.getName());
        assertEquals("health facility regional name", healthFacility.getRegionalName());
        assertNotNull(healthFacility.getHealthBlock());
        assertNotNull(healthFacility.getHealthFacilityType());

        healthSubFacilityImportService.importData(read("csv/health_sub_facility.csv"));
        HealthSubFacility healthSubFacility = healthSubFacilityDataService.findByCode(8L);
        assertNotNull(healthSubFacility);
        assertEquals(8L, ((long) healthSubFacility.getCode()));
        assertEquals("health sub facility name", healthSubFacility.getName());
        assertEquals("health sub facility regional name", healthSubFacility.getRegionalName());
        assertNotNull(healthSubFacility.getHealthFacility());
    }

    @Ignore //TEMP(expected = IllegalArgumentException.class)
    public void testThrowExceptionForMalformedCsv() throws Exception {
        districtImportService.importData(read("csv/district_malformed.csv"));
    }

    @Ignore //TEMP(expected = CsvImportDataException.class)
    public void testThrowExceptionForInvalidCellFormat() throws Exception {
        districtImportService.importData(read("csv/district_invalid_cell_format.csv"));
    }

    @Ignore //TEMP
    public void testRollbackAllAfterSingleFailure() throws Exception {
        boolean thrown = false;
        try {
            districtImportService.importData(read("csv/district_rollback.csv"));
        } catch (Exception e) {
            thrown = true;
        }
        assertTrue(thrown);
        assertNull(districtDataService.findByCode(1002L));
        assertNull(districtDataService.findByCode(1003L));
        assertNull(districtDataService.findByCode(1004L));
    }

    private Reader read(String resource) {
        return new InputStreamReader(getClass().getClassLoader().getResourceAsStream(resource));
    }
}
