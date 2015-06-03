package org.motechproject.nms.testing.it.flw;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.nms.flw.domain.FrontLineWorker;
import org.motechproject.nms.flw.domain.FrontLineWorkerStatus;
import org.motechproject.nms.flw.repository.FrontLineWorkerDataService;
import org.motechproject.nms.flw.service.FrontLineWorkerImportService;
import org.motechproject.nms.region.domain.Circle;
import org.motechproject.nms.region.domain.District;
import org.motechproject.nms.region.domain.Language;
import org.motechproject.nms.region.domain.LanguageLocation;
import org.motechproject.nms.region.domain.State;
import org.motechproject.nms.region.repository.CircleDataService;
import org.motechproject.nms.region.repository.DistrictDataService;
import org.motechproject.nms.region.repository.HealthBlockDataService;
import org.motechproject.nms.region.repository.HealthFacilityDataService;
import org.motechproject.nms.region.repository.HealthFacilityTypeDataService;
import org.motechproject.nms.region.repository.HealthSubFacilityDataService;
import org.motechproject.nms.region.repository.LanguageDataService;
import org.motechproject.nms.region.repository.LanguageLocationDataService;
import org.motechproject.nms.region.repository.NationalDefaultLanguageLocationDataService;
import org.motechproject.nms.region.repository.StateDataService;
import org.motechproject.nms.region.repository.TalukaDataService;
import org.motechproject.nms.region.repository.VillageDataService;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.motechproject.nms.testing.it.utils.LocationDataUtils.createCircle;
import static org.motechproject.nms.testing.it.utils.LocationDataUtils.createDistrict;
import static org.motechproject.nms.testing.it.utils.LocationDataUtils.createLanguage;
import static org.motechproject.nms.testing.it.utils.LocationDataUtils.createLanguageLocation;
import static org.motechproject.nms.testing.it.utils.LocationDataUtils.createState;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@ExamFactory(MotechNativeTestContainerFactory.class)
public class FrontLineWorkerImportServiceBundleIT extends BasePaxIT {

    @Inject
    private NationalDefaultLanguageLocationDataService nationalDefaultLanguageLocationDataService;
    @Inject
    private LanguageDataService languageDataService;
    @Inject
    private LanguageLocationDataService languageLocationDataService;
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
    private CircleDataService circleDataService;
    @Inject
    private FrontLineWorkerDataService frontLineWorkerDataService;

    @Inject
    private FrontLineWorkerImportService frontLineWorkerImportService;

    @Before
    public void setUp() {
        for (FrontLineWorker flw: frontLineWorkerDataService.retrieveAll()) {
            flw.setStatus(FrontLineWorkerStatus.INVALID);
            flw.setInvalidationDate(new DateTime().withDate(2011, 8, 1));

            frontLineWorkerDataService.update(flw);
        }
        frontLineWorkerDataService.deleteAll();
        nationalDefaultLanguageLocationDataService.deleteAll();
        languageLocationDataService.deleteAll();
        languageDataService.deleteAll();
        healthSubFacilityDataService.deleteAll();
        healthFacilityDataService.deleteAll();
        healthFacilityTypeDataService.deleteAll();
        healthBlockDataService.deleteAll();
        villageDataService.deleteAll();
        talukaDataService.deleteAll();
        districtDataService.deleteAll();
        stateDataService.deleteAll();
        circleDataService.deleteAll();

        State state1 = createState(1L, "State 1");
        District district11 = createDistrict(state1, 11L, "District 11");
        District district12 = createDistrict(state1, 12L, "District 12");
        state1.getDistricts().addAll(Arrays.asList(district11, district12));
        stateDataService.create(state1);

        Language lang1 = createLanguage("Lang 1");
        languageDataService.create(lang1);

        Circle circle1 = createCircle("Circle 1");
        LanguageLocation llc11 = createLanguageLocation("LLC 11", lang1, circle1, false, district11);
        circle1.getStates().addAll(Arrays.asList(state1));
        circle1.getLanguageLocations().addAll(Arrays.asList(llc11));
        circleDataService.create(circle1);
    }

    @Test
    public void testImportWhenDistrictLanguageLocationPresent() throws Exception {
        Reader reader = createReaderWithHeaders("#0,1234567890,FLW 0,11");
        frontLineWorkerImportService.importData(reader);

        FrontLineWorker flw = frontLineWorkerDataService.findByContactNumber(1234567890L);
        assertFLW(flw, "#0", 1234567890L, "FLW 0", "District 11", "LLC 11");
    }

    @Test
    public void testImportWhenDistrictLanguageLocationNotPresent() throws Exception {
        Reader reader = createReaderWithHeaders("#0,1234567890,FLW 0,12");
        frontLineWorkerImportService.importData(reader);

        FrontLineWorker flw = frontLineWorkerDataService.findByContactNumber(1234567890L);
        assertFLW(flw, "#0", 1234567890L, "FLW 0", "District 12", null);
    }

    @Test
    public void testImportWhenDistrictNotPresent() throws Exception {
        Reader reader = createReaderWithHeaders("#0,1234567890,FLW 0,");
        frontLineWorkerImportService.importData(reader);

        FrontLineWorker flw = frontLineWorkerDataService.findByContactNumber(1234567890L);
        assertFLW(flw, "#0", 1234567890L, "FLW 0", null, null);
    }

    private void assertFLW(FrontLineWorker flw, String mctsFlwId, long contactNumber, String name, String districtName, String languageLocationCode) {
        assertNotNull(flw);
        assertEquals(mctsFlwId, flw.getMctsFlwId());
        assertEquals(contactNumber, (long) flw.getContactNumber());
        assertEquals(name, flw.getName());
        assertEquals(districtName, null != flw.getDistrict() ? flw.getDistrict().getName() : null);
        assertEquals(languageLocationCode, null != flw.getLanguageLocation() ? flw.getLanguageLocation().getCode() : null);
    }

    private Reader createReaderWithHeaders(String... lines) {
        StringBuilder builder = new StringBuilder();
        builder.append("ID,Contact_No,Name,District_ID").append("\n");
        for (String line : lines) {
            builder.append(line).append("\n");
        }
        return new StringReader(builder.toString());
    }
}
