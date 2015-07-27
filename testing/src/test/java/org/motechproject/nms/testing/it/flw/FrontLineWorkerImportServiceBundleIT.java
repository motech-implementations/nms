package org.motechproject.nms.testing.it.flw;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.nms.csv.exception.CsvImportDataException;
import org.motechproject.nms.flw.domain.FrontLineWorker;
import org.motechproject.nms.flw.domain.FrontLineWorkerStatus;
import org.motechproject.nms.flw.repository.FrontLineWorkerDataService;
import org.motechproject.nms.flw.service.FrontLineWorkerImportService;
import org.motechproject.nms.flw.service.FrontLineWorkerService;
import org.motechproject.nms.region.domain.Circle;
import org.motechproject.nms.region.domain.District;
import org.motechproject.nms.region.domain.HealthBlock;
import org.motechproject.nms.region.domain.HealthFacility;
import org.motechproject.nms.region.domain.HealthFacilityType;
import org.motechproject.nms.region.domain.HealthSubFacility;
import org.motechproject.nms.region.domain.Language;
import org.motechproject.nms.region.domain.State;
import org.motechproject.nms.region.domain.Taluka;
import org.motechproject.nms.region.domain.Village;
import org.motechproject.nms.region.repository.CircleDataService;
import org.motechproject.nms.region.repository.DistrictDataService;
import org.motechproject.nms.region.repository.LanguageDataService;
import org.motechproject.nms.region.repository.StateDataService;
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
import java.io.StringReader;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.motechproject.nms.testing.it.utils.RegionHelper.createCircle;
import static org.motechproject.nms.testing.it.utils.RegionHelper.createDistrict;
import static org.motechproject.nms.testing.it.utils.RegionHelper.createHealthBlock;
import static org.motechproject.nms.testing.it.utils.RegionHelper.createHealthFacility;
import static org.motechproject.nms.testing.it.utils.RegionHelper.createHealthFacilityType;
import static org.motechproject.nms.testing.it.utils.RegionHelper.createHealthSubFacility;
import static org.motechproject.nms.testing.it.utils.RegionHelper.createLanguage;
import static org.motechproject.nms.testing.it.utils.RegionHelper.createState;
import static org.motechproject.nms.testing.it.utils.RegionHelper.createTaluka;
import static org.motechproject.nms.testing.it.utils.RegionHelper.createVillage;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@ExamFactory(MotechNativeTestContainerFactory.class)
public class FrontLineWorkerImportServiceBundleIT extends BasePaxIT {

    @Inject
    LanguageDataService languageDataService;
    @Inject
    StateDataService stateDataService;
    @Inject
    DistrictDataService districtDataService;
    @Inject
    CircleDataService circleDataService;
    @Inject
    FrontLineWorkerDataService frontLineWorkerDataService;
    @Inject
    TestingService testingService;
    @Inject
    FrontLineWorkerService frontLineWorkerService;

    @Inject
    FrontLineWorkerImportService frontLineWorkerImportService;

    @Before
    public void setUp() {
        testingService.clearDatabase();

        Language lang1 = createLanguage("L1", "Lang 1");
        languageDataService.create(lang1);

        State state1 = createState(1L, "State 1");
        District district11 = createDistrict(state1, 11L, "District 11", lang1);
        District district12 = createDistrict(state1, 12L, "District 12");

        District kuraput = createDistrict(state1, 29L, "Koraput");
        District kandhamal = createDistrict(state1, 21L, "Kandhamal");
        District ganjam = createDistrict(state1, 19L, "Ganjam");
        District jharsuguda = createDistrict(state1, 2L, "Jharsuguda");
        District bargarh = createDistrict(state1, 1L, "Bargarh");
        District puri = createDistrict(state1, 18L, "Puri");

        Taluka similiguda = createTaluka(kuraput, "0463", "Similiguda", 1);
        Taluka Phulabani = createTaluka(kandhamal, "0360", "Phulabani Town", 1);
        Taluka kotagarh = createTaluka(kandhamal, "0371", "Kotagarh", 1);
        Taluka digapahandi = createTaluka(ganjam, "0343", "DIGAPAHANDI", 1);
        Taluka lakhanpur = createTaluka(jharsuguda, "0017", "Lakhanpur P.S.", 1);
        Taluka baliguda = createTaluka(kandhamal, "0367", "Baliguda", 1);
        Taluka bhatli = createTaluka(bargarh, "0013", "Bhatli", 1);
        Taluka pipili = createTaluka(puri, "0304", "Pipili", 1);

        HealthBlock kunduli = createHealthBlock(similiguda, 405L, "Kunduli", "hq");
        HealthBlock Phulbani  = createHealthBlock(Phulabani, 348L, "Phulbani", "hq");
        HealthBlock Kotagarh  = createHealthBlock(kotagarh, 337L, "Kotagarh", "hq");
        HealthBlock DIGAPAHANDI  = createHealthBlock(digapahandi, 275L, "DIGAPAHANDI", "hq");
        HealthBlock Lakhanpur  = createHealthBlock(lakhanpur, 262L, "Lakhanpur", "hq");
        HealthBlock Baliguda  = createHealthBlock(baliguda, 317L, "Baliguda", "hq");
        HealthBlock BHATLI  = createHealthBlock(bhatli, 175L, "BHATLI", "hq");
        HealthBlock Pipli  = createHealthBlock(pipili, 432L, "Pipli", "hq");

        HealthFacilityType hft = createHealthFacilityType("Type", 1L);

        HealthFacility CHC_Kunduli = createHealthFacility(kunduli, 1238L, "CHC Kunduli", hft);
        HealthFacility PHC_N_Katringia = createHealthFacility(Phulbani, 1427L, "PHC (N) Katringia", hft);
        HealthFacility CHC_Subarnagiri = createHealthFacility(Kotagarh, 1393L, "CHC Subarnagiri", hft);
        HealthFacility BHISMAGIRI_PHC = createHealthFacility(DIGAPAHANDI, 1799L, "BHISMAGIRI PHC-N", hft);
        HealthFacility Lakhanpur_CHC = createHealthFacility(Lakhanpur, 689L, "Lakhanpur CHC", hft);
        HealthFacility CHC_Barakhama = createHealthFacility(Baliguda, 1271L, "CHC Barakhama", hft);
        HealthFacility BHATLI_CHC = createHealthFacility(BHATLI, 250L, "BHATLI CHC", hft);
        HealthFacility Managalpur_CHC = createHealthFacility(Pipli, 1441L, "Managalpur CHC", hft);

        HealthSubFacility Dhudhari = createHealthSubFacility("Dhudhari", 4434L, CHC_Kunduli);
        HealthSubFacility Adari = createHealthSubFacility("Adari", 5130L, CHC_Subarnagiri);
        HealthSubFacility Kusuraloi = createHealthSubFacility("Kusuraloi", 2141L, Lakhanpur_CHC);
        HealthSubFacility Rampela = createHealthSubFacility("Rampela", 2142L, Lakhanpur_CHC);
        HealthSubFacility Kutikia_MCH = createHealthSubFacility("Kutikia MCH", 4705L, CHC_Barakhama);
        HealthSubFacility BHATLI_SC = createHealthSubFacility("BHATLI SC", 1096L, BHATLI_CHC);
        HealthSubFacility Bharatipur_SC = createHealthSubFacility("Bharatipur SC", 5159L, Managalpur_CHC);
        HealthSubFacility Danagahiri_SC = createHealthSubFacility("Danagahiri SC", 5167L, Managalpur_CHC);

        Village Bharatipur = createVillage(pipili, 0L, 28981L, "Bharatipur(28981)");
        Village Nuasahi = createVillage(pipili, 0L, 10005284L, "Nuasahi *");

        similiguda.getHealthBlocks().add(kunduli);
        Phulabani.getHealthBlocks().add(Phulbani);
        kotagarh.getHealthBlocks().add(Kotagarh);
        digapahandi.getHealthBlocks().add(DIGAPAHANDI);
        lakhanpur.getHealthBlocks().add(Lakhanpur);
        baliguda.getHealthBlocks().add(Baliguda);
        bhatli.getHealthBlocks().add(BHATLI);
        pipili.getHealthBlocks().add(Pipli);

        kunduli.getHealthFacilities().add(CHC_Kunduli);
        Phulbani.getHealthFacilities().add(PHC_N_Katringia);
        Kotagarh.getHealthFacilities().add(CHC_Subarnagiri);
        DIGAPAHANDI.getHealthFacilities().add(BHISMAGIRI_PHC);
        Lakhanpur.getHealthFacilities().add(Lakhanpur_CHC);
        Baliguda.getHealthFacilities().add(CHC_Barakhama);
        BHATLI.getHealthFacilities().add(BHATLI_CHC);
        Pipli.getHealthFacilities().add(Managalpur_CHC);

        CHC_Kunduli.getHealthSubFacilities().add(Dhudhari);
        CHC_Subarnagiri.getHealthSubFacilities().add(Adari);
        Lakhanpur_CHC.getHealthSubFacilities().addAll(Arrays.asList(Kusuraloi, Rampela));
        CHC_Barakhama.getHealthSubFacilities().add(Kutikia_MCH);
        BHATLI_CHC.getHealthSubFacilities().add(BHATLI_SC);
        Managalpur_CHC.getHealthSubFacilities().addAll(Arrays.asList(Bharatipur_SC, Danagahiri_SC));

        pipili.getVillages().addAll(Arrays.asList(Bharatipur, Nuasahi));

        kuraput.getTalukas().add(similiguda);
        kandhamal.getTalukas().addAll(Arrays.asList(Phulabani, kotagarh, baliguda));
        ganjam.getTalukas().add(digapahandi);
        jharsuguda.getTalukas().add(lakhanpur);
        bargarh.getTalukas().add(bhatli);
        puri.getTalukas().add(pipili);

        state1.getDistricts().addAll(Arrays.asList(district11, district12,
                kuraput, kandhamal, ganjam, jharsuguda, bargarh, puri));
        stateDataService.create(state1);

        Circle circle1 = createCircle("Circle 1");
        circle1.getStates().addAll(Arrays.asList(state1));
        circleDataService.create(circle1);
    }

    // This test should load the FLW with MCTS id '#1' and attempt to update their MSISDN to a number already
    // in use.  This should result in a unique constraint exception
    @Test(expected = CsvImportDataException.class)
    public void testImportMSISDNConflict() throws Exception {
        State state = stateDataService.findByName("State 1");
        District district = state.getDistricts().get(0);

        FrontLineWorker flw = new FrontLineWorker("Existing With MSISDN", 1234567890L);
        flw.setMctsFlwId("#0");
        flw.setState(state);
        flw.setDistrict(district);
        frontLineWorkerService.add(flw);

        flw = new FrontLineWorker("Will Update Conflict MSISDN", 1111111111L);
        flw.setMctsFlwId("#1");
        flw.setState(state);
        flw.setDistrict(district);
        frontLineWorkerService.add(flw);

        Reader reader = createReaderWithHeaders("#1\t1234567890\tFLW 0\t11");
        frontLineWorkerImportService.importData(reader);
    }

    // This test should load the FLW with MSISDN 1234567890 however that FLW already has a different MCTS ID
    // assigned to them.  This should result in an exception
    @Test(expected = CsvImportDataException.class)
    public void testImportByMSISDNConflictWithMCTSId() throws Exception {
        FrontLineWorker flw = new FrontLineWorker("Frank Lloyd Wright", 1234567890L);
        flw.setMctsFlwId("#0");
        frontLineWorkerService.add(flw);

        Reader reader = createReaderWithHeaders("#1\t1234567890\tFLW 0\t11");
        frontLineWorkerImportService.importData(reader);
    }

    /**
     * VerifyFT513  verify that status of flw must be set to "inactive" when the flw data is imported into
     * the NMS DB and the user has not yet called
     */
    @Test
    public void testImportWhenDistrictLanguageLocationPresent() throws Exception {
        Reader reader = createReaderWithHeaders("#0\t1234567890\tFLW 0\t11");
        frontLineWorkerImportService.importData(reader);

        FrontLineWorker flw = frontLineWorkerDataService.findByContactNumber(1234567890L);
        assertFLW(flw, "#0", 1234567890L, "FLW 0", "District 11", "L1");
        assertEquals(FrontLineWorkerStatus.INACTIVE, flw.getStatus());
    }

    @Test
    public void testImportWhenDistrictLanguageLocationNotPresent() throws Exception {
        Reader reader = createReaderWithHeaders("#0\t1234567890\tFLW 0\t12");
        frontLineWorkerImportService.importData(reader);

        FrontLineWorker flw = frontLineWorkerDataService.findByContactNumber(1234567890L);
        assertFLW(flw, "#0", 1234567890L, "FLW 0", "District 12", null);
    }

    @Test(expected = CsvImportDataException.class)
    public void testImportWhenDistrictNotPresent() throws Exception {
        Reader reader = createReaderWithHeaders("#0\t1234567890\tFLW 0\t");
        frontLineWorkerImportService.importData(reader);
    }

    private void assertFLW(FrontLineWorker flw, String mctsFlwId, long contactNumber, String name, String districtName, String languageLocationCode) {
        assertNotNull(flw);
        assertEquals(mctsFlwId, flw.getMctsFlwId());
        assertEquals(contactNumber, (long) flw.getContactNumber());
        assertEquals(name, flw.getName());
        assertEquals(districtName, null != flw.getDistrict() ? flw.getDistrict().getName() : null);
        assertEquals(languageLocationCode, null != flw.getLanguage() ? flw.getLanguage().getCode() : null);
    }

    @Test
    public void testImportFromSampleDataFile() throws Exception {
        frontLineWorkerImportService.importData(read("csv/anm-asha.txt"));

        FrontLineWorker flw1 = frontLineWorkerDataService.findByContactNumber(9999999996L);
        assertFLW(flw1, "72185", 9999999996L, "Bishnu Priya Behera", "Koraput", null);
    }

    private Reader createReaderWithHeaders(String... lines) {
        StringBuilder builder = new StringBuilder();
        builder.append("\n");
        builder.append("State Name : State 1").append("\n");
        builder.append("\n");
        builder.append("ID\tContact_No\tName\tDistrict_ID").append("\n");
        for (String line : lines) {
            builder.append(line).append("\n");
        }
        return new StringReader(builder.toString());
    }

    private Reader read(String resource) {
        return new InputStreamReader(getClass().getClassLoader().getResourceAsStream(resource));
    }

}
