package org.motechproject.nms.testing.it.flw;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.nms.csv.exception.CsvImportDataException;
import org.motechproject.nms.flw.domain.FrontLineWorker;
import org.motechproject.nms.flw.repository.FrontLineWorkerDataService;
import org.motechproject.nms.flw.service.FrontLineWorkerService;
import org.motechproject.nms.flw.service.FrontLineWorkerUpdateLanguageImportService;
import org.motechproject.nms.region.repository.CircleDataService;
import org.motechproject.nms.region.repository.DistrictDataService;
import org.motechproject.nms.region.repository.LanguageDataService;
import org.motechproject.nms.region.repository.StateDataService;
import org.motechproject.nms.testing.it.utils.RegionHelper;
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

import static org.junit.Assert.assertEquals;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@ExamFactory(MotechNativeTestContainerFactory.class)
public class FrontLineWorkerUpdateLanguageServiceBundleIT extends BasePaxIT {

    @Inject
    private CircleDataService circleDataService;

    @Inject
    private DistrictDataService districtDataService;

    @Inject
    private StateDataService stateDataService;

    @Inject
    private LanguageDataService languageDataService;

    @Inject
    private FrontLineWorkerDataService frontLineWorkerDataService;

    @Inject
    private FrontLineWorkerService frontLineWorkerService;

    @Inject
    private TestingService testingService;

    @Inject
    private FrontLineWorkerUpdateLanguageImportService flwUpdateLanguageImportService;

    private RegionHelper rh;
    private String resource;

    @Before
    public void setUp() {
        rh = new RegionHelper(languageDataService, circleDataService, stateDataService, districtDataService);

        testingService.clearDatabase();

        rh.hindiLanguage();
        rh.kannadaLanguage();
    }

    // Test when language not provided
    @Test(expected = CsvImportDataException.class)
    public void testImportWhenLanguageNotPresent() throws Exception {
        Reader reader = createReaderWithHeaders("72185,210302604211400029,9439986187,");
        flwUpdateLanguageImportService.importData(reader);
    }

    // Test when language not in database
    @Test(expected = CsvImportDataException.class)
    public void testImportWhenLanguageNotInDatabase() throws Exception {
        FrontLineWorker flw = new FrontLineWorker(9439986187L);
        frontLineWorkerService.add(flw);

        Reader reader = createReaderWithHeaders("72185,210302604211400029,9439986187,en");
        flwUpdateLanguageImportService.importData(reader);
    }

    // Test when only NMS Id found and FLW not in database
    @Test(expected = CsvImportDataException.class)
    public void testImportWhenFLWIdProvidedButNotInDatabase() throws Exception {
        Reader reader = createReaderWithHeaders("72185,,,hi");
        flwUpdateLanguageImportService.importData(reader);
    }

    // Test when only MCTS Id found and FLW not in database
    @Test(expected = CsvImportDataException.class)
    public void testImportWhenMCTSIdProvidedButNotInDatabase() throws Exception {
        Reader reader = createReaderWithHeaders(",210302604211400029,,hi");
        flwUpdateLanguageImportService.importData(reader);
    }
    // Test when only MSISDN found and FLW not in database
    @Test(expected = CsvImportDataException.class)
    public void testImportWhenMSISDProvidedButNotInDatabase() throws Exception {
        Reader reader = createReaderWithHeaders(",,9439986187,hi");
        flwUpdateLanguageImportService.importData(reader);
    }

    // Test NMS Id takes precedence over MCTS ID
    @Test
    public void testImportWhenNMSIdTakesPrecedenceOverMCTSId() throws Exception {
        FrontLineWorker flw = new FrontLineWorker(1000000000L);
        flw.setFlwId("72185");
        flw.setLanguage(rh.kannadaLanguage());
        frontLineWorkerService.add(flw);

        flw = new FrontLineWorker(2000000000L);
        flw.setMctsFlwId("210302604211400029");
        flw.setLanguage(rh.kannadaLanguage());
        frontLineWorkerService.add(flw);

        Reader reader = createReaderWithHeaders("72185,210302604211400029,,hi");
        flwUpdateLanguageImportService.importData(reader);

        flw = frontLineWorkerDataService.findByContactNumber(1000000000L);
        assertEquals(rh.hindiLanguage(), flw.getLanguage());

        flw = frontLineWorkerDataService.findByContactNumber(2000000000L);
        assertEquals(rh.kannadaLanguage(), flw.getLanguage());
    }

    // Test NMS Id takes precedence over MSISDN
    @Test
    public void testImportWhenNMSIdTakesPrecedenceOverMSIDN() throws Exception {
        FrontLineWorker flw = new FrontLineWorker(1000000000L);
        flw.setFlwId("72185");
        flw.setLanguage(rh.kannadaLanguage());
        frontLineWorkerService.add(flw);

        flw = new FrontLineWorker(2000000000L);
        flw.setLanguage(rh.kannadaLanguage());
        frontLineWorkerService.add(flw);

        Reader reader = createReaderWithHeaders("72185,,2000000000,hi");
        flwUpdateLanguageImportService.importData(reader);

        flw = frontLineWorkerDataService.findByContactNumber(1000000000L);
        assertEquals(rh.hindiLanguage(), flw.getLanguage());

        flw = frontLineWorkerDataService.findByContactNumber(2000000000L);
        assertEquals(rh.kannadaLanguage(), flw.getLanguage());
    }

    // Test MCTS Id takes precedence over MSISDN
    @Test
    public void testImportWhenMCTSIdTakesPrecedenceOverMSIDN() throws Exception {
        FrontLineWorker flw = new FrontLineWorker(1000000000L);
        flw.setMctsFlwId("210302604211400029");
        flw.setLanguage(rh.kannadaLanguage());
        frontLineWorkerService.add(flw);

        flw = new FrontLineWorker(2000000000L);
        flw.setLanguage(rh.kannadaLanguage());
        frontLineWorkerService.add(flw);

        Reader reader = createReaderWithHeaders("72185,210302604211400029,2000000000,hi");
        flwUpdateLanguageImportService.importData(reader);

        flw = frontLineWorkerDataService.findByContactNumber(1000000000L);
        assertEquals(rh.hindiLanguage(), flw.getLanguage());

        flw = frontLineWorkerDataService.findByContactNumber(2000000000L);
        assertEquals(rh.kannadaLanguage(), flw.getLanguage());
    }

    // Test MSISDN only
    @Test
    public void testImportWhenMSISDNOnly() throws Exception {
        FrontLineWorker flw = new FrontLineWorker(1000000000L);
        flw.setLanguage(rh.kannadaLanguage());
        frontLineWorkerService.add(flw);

        flw = new FrontLineWorker(2000000000L);
        flw.setLanguage(rh.kannadaLanguage());
        frontLineWorkerService.add(flw);

        Reader reader = createReaderWithHeaders("72185,210302604211400029,1000000000,hi");
        flwUpdateLanguageImportService.importData(reader);

        flw = frontLineWorkerDataService.findByContactNumber(1000000000L);
        assertEquals(rh.hindiLanguage(), flw.getLanguage());

        flw = frontLineWorkerDataService.findByContactNumber(2000000000L);
        assertEquals(rh.kannadaLanguage(), flw.getLanguage());
    }

    @Test
    public void testImportFromSampleDataFile() throws Exception {
        FrontLineWorker flw = new FrontLineWorker(1000000000L);
        flw.setLanguage(rh.kannadaLanguage());
        flw.setFlwId("72185");
        frontLineWorkerService.add(flw);

        flw = new FrontLineWorker(2000000000L);
        flw.setLanguage(rh.kannadaLanguage());
        frontLineWorkerService.add(flw);

        flwUpdateLanguageImportService.importData(read("csv/flw_language_update.csv"));

        flw = frontLineWorkerDataService.findByContactNumber(1000000000L);
        assertEquals(rh.hindiLanguage(), flw.getLanguage());

        flw = frontLineWorkerDataService.findByContactNumber(2000000000L);
        assertEquals(rh.hindiLanguage(), flw.getLanguage());
    }

    private Reader createReaderWithHeaders(String... lines) {
        StringBuilder builder = new StringBuilder();
        builder.append("NMS FLW-ID,MCTS FLW-ID,MSISDN,LANGUAGE CODE").append("\n");
        for (String line : lines) {
            builder.append(line).append("\n");
        }
        return new StringReader(builder.toString());
    }

    private Reader read(String resource) {
        return new InputStreamReader(getClass().getClassLoader().getResourceAsStream(resource));
    }
}