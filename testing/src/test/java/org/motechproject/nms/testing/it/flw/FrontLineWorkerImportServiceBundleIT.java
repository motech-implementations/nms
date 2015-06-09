package org.motechproject.nms.testing.it.flw;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.nms.flw.domain.FrontLineWorker;
import org.motechproject.nms.flw.repository.FrontLineWorkerDataService;
import org.motechproject.nms.flw.service.FrontLineWorkerImportService;
import org.motechproject.nms.region.domain.Circle;
import org.motechproject.nms.region.domain.District;
import org.motechproject.nms.region.domain.Language;
import org.motechproject.nms.region.domain.State;
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
import static org.motechproject.nms.testing.it.utils.LocationDataUtils.createCircle;
import static org.motechproject.nms.testing.it.utils.LocationDataUtils.createDistrict;
import static org.motechproject.nms.testing.it.utils.LocationDataUtils.createLanguage;
import static org.motechproject.nms.testing.it.utils.LocationDataUtils.createState;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@ExamFactory(MotechNativeTestContainerFactory.class)
public class FrontLineWorkerImportServiceBundleIT extends BasePaxIT {

    @Inject
    private LanguageDataService languageDataService;
    @Inject
    private StateDataService stateDataService;
    @Inject
    private DistrictDataService districtDataService;
    @Inject
    private CircleDataService circleDataService;
    @Inject
    private FrontLineWorkerDataService frontLineWorkerDataService;
    @Inject
    private TestingService testingService;

    @Inject
    private FrontLineWorkerImportService frontLineWorkerImportService;

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

        state1.getDistricts().addAll(Arrays.asList(district11, district12,
                kuraput, kandhamal, ganjam, jharsuguda, bargarh, puri));
        stateDataService.create(state1);

        Circle circle1 = createCircle("Circle 1");
        circle1.getStates().addAll(Arrays.asList(state1));
        circleDataService.create(circle1);
    }

    @Test
    public void testImportWhenDistrictLanguageLocationPresent() throws Exception {
        Reader reader = createReaderWithHeaders("#0\t1234567890\tFLW 0\t11");
        frontLineWorkerImportService.importData(reader);

        FrontLineWorker flw = frontLineWorkerDataService.findByContactNumber(1234567890L);
        assertFLW(flw, "#0", 1234567890L, "FLW 0", "District 11", "L1");
    }

    @Test
    public void testImportWhenDistrictLanguageLocationNotPresent() throws Exception {
        Reader reader = createReaderWithHeaders("#0\t1234567890\tFLW 0\t12");
        frontLineWorkerImportService.importData(reader);

        FrontLineWorker flw = frontLineWorkerDataService.findByContactNumber(1234567890L);
        assertFLW(flw, "#0", 1234567890L, "FLW 0", "District 12", null);
    }

    @Test
    public void testImportWhenDistrictNotPresent() throws Exception {
        Reader reader = createReaderWithHeaders("#0\t1234567890\tFLW 0\t");
        frontLineWorkerImportService.importData(reader);

        FrontLineWorker flw = frontLineWorkerDataService.findByContactNumber(1234567890L);
        assertFLW(flw, "#0", 1234567890L, "FLW 0", null, null);
    }

    @Test
    public void testImportFromSampleDataFile() throws Exception {
        frontLineWorkerImportService.importData(read("csv/anm-asha.txt"));

        FrontLineWorker flw1 = frontLineWorkerDataService.findByContactNumber(9999999996L);
        assertFLW(flw1, "72185", 9999999996L, "Bishnu Priya Behera", "Koraput", null);
    }

    private void assertFLW(FrontLineWorker flw, String mctsFlwId, long contactNumber, String name, String districtName, String languageLocationCode) {
        assertNotNull(flw);
        assertEquals(mctsFlwId, flw.getMctsFlwId());
        assertEquals(contactNumber, (long) flw.getContactNumber());
        assertEquals(name, flw.getName());
        assertEquals(districtName, null != flw.getDistrict() ? flw.getDistrict().getName() : null);
        assertEquals(languageLocationCode, null != flw.getLanguage() ? flw.getLanguage().getCode() : null);
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
