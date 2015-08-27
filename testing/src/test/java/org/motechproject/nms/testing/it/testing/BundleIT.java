package org.motechproject.nms.testing.it.testing;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.mds.filter.Filter;
import org.motechproject.mds.filter.Filters;
import org.motechproject.nms.kilkari.service.MctsBeneficiaryImportService;
import org.motechproject.nms.region.domain.District;
import org.motechproject.nms.region.domain.Language;
import org.motechproject.nms.region.domain.State;
import org.motechproject.nms.region.repository.DistrictDataService;
import org.motechproject.nms.region.repository.LanguageDataService;
import org.motechproject.nms.region.repository.StateDataService;
import org.motechproject.nms.testing.it.utils.Timer;
import org.motechproject.nms.testing.service.TestingService;
import org.motechproject.testing.osgi.BasePaxIT;
import org.motechproject.testing.osgi.container.MotechNativeTestContainerFactory;
import org.ops4j.pax.exam.ExamFactory;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;

import javax.inject.Inject;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import static org.junit.Assert.assertNotNull;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@ExamFactory(value = MotechNativeTestContainerFactory.class)
public class BundleIT extends BasePaxIT {

    public static final int TEST_COUNT = 1000;
    @Inject
    TestingService testingService;

    @Inject
    LanguageDataService languageDataService;

    @Inject
    StateDataService stateDataService;

    @Inject
    DistrictDataService districtDataService;

    @Inject
    MctsBeneficiaryImportService mctsBeneficiaryImportService;


    @Test
    public void testNothingImportant() {
        assertNotNull(testingService);
    }


    private void createLocationData() {
        Language language = new Language("HI", "Hindi");
        if (languageDataService.countForFilters(new Filters(new Filter("code", "HI"))) == 0) {
            languageDataService.create(language);
        }

        State state = new State("Delhi", 1L);
        //the \1 is a hack - Filter should take a Long constructor
        if (stateDataService.countForFilters(new Filters(new Filter("code", "\1"))) == 0) {
                stateDataService.create(state);
        }

        District district = new District();
        district.setState(state);
        district.setCode(2L);
        district.setName("Delhi District");
        district.setRegionalName("Delhi District");
        //the \2 is a hack - Filter should take a Long constructor
        if (districtDataService.countForFilters(new Filters(new Filter("code", "\2"))) == 0) {
           districtDataService.create(district);
        }
    }


    @Test
    public void testTheRealDeal() throws IOException {
        Timer timer = new Timer();
        testingService.clearDatabase();
        getLogger().debug("clearDatabase: {}", timer.time());

        timer.reset();
        testingService.createSubscriptionPacks();
        getLogger().debug("createSubscriptionPacks: {}", timer.time());

        timer.reset();
        createLocationData();
        getLogger().debug("createLocationData: {}", timer.time());

        timer = new Timer("mom", "moms");
        String file = testingService.createMctsMoms(TEST_COUNT).split("\t")[0];
        getLogger().debug("Created {}", timer.frequency(TEST_COUNT));

        timer.reset();
        mctsBeneficiaryImportService.importMotherData(new InputStreamReader(new FileInputStream(file)));
        getLogger().debug("Imported {}", timer.frequency(TEST_COUNT));
    }
}
