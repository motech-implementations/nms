package org.motechproject.nms.testing.it.region;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.motechproject.nms.region.domain.Circle;
import org.motechproject.nms.region.domain.District;
import org.motechproject.nms.region.domain.Language;
import org.motechproject.nms.region.domain.NationalDefaultLanguage;
import org.motechproject.nms.region.domain.State;
import org.motechproject.nms.region.repository.CircleDataService;
import org.motechproject.nms.region.repository.DistrictDataService;
import org.motechproject.nms.region.repository.LanguageDataService;
import org.motechproject.nms.region.repository.NationalDefaultLanguageDataService;
import org.motechproject.nms.region.repository.StateDataService;
import org.motechproject.testing.osgi.BasePaxIT;
import org.motechproject.testing.osgi.container.MotechNativeTestContainerFactory;
import org.ops4j.pax.exam.ExamFactory;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;

import javax.inject.Inject;
import javax.jdo.JDODataStoreException;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@ExamFactory(MotechNativeTestContainerFactory.class)
public class NationalDefaultLanguageLocationBundleIT extends BasePaxIT{

    @Inject
    private NationalDefaultLanguageDataService nationalDefaultLanguageLocationDataService;

    @Inject
    private StateDataService stateDataService;

    @Inject
    private DistrictDataService districtDataService;

    @Inject
    private CircleDataService circleDataService;

    @Inject
    private LanguageDataService languageDataService;

    private void cleanAllData() {
        nationalDefaultLanguageLocationDataService.deleteAll();
        languageDataService.deleteAll();
        districtDataService.deleteAll();
        stateDataService.deleteAll();
        circleDataService.deleteAll();
    }

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void testVerifyOnlyOneNationalDefaultLanguage() {
        cleanAllData();

        District district = new District();
        district.setName("District 1");
        district.setRegionalName("District 1");
        district.setCode(1L);

        State state = new State();
        state.setName("State 1");
        state.setCode(1L);
        state.getDistricts().add(district);

        stateDataService.create(state);

        District district2 = new District();
        district2.setName("District 2");
        district2.setRegionalName("District 2");
        district2.setCode(2L);

        State state2 = new State();
        state2.setName("State 2");
        state2.setCode(2L);
        state2.getDistricts().add(district2);

        stateDataService.create(state2);

        Language ta = languageDataService.create(new Language("50", "tamil"));
        Language hi = languageDataService.create(new Language("99", "hindi"));

        Circle circle = new Circle("AA");
        circle.setDefaultLanguage(ta);
        circleDataService.update(circle);

        district.setLanguage(ta);
        districtDataService.update(district);

        district2.setLanguage(hi);
        districtDataService.update(district2);

        nationalDefaultLanguageLocationDataService.create(new NationalDefaultLanguage(ta));

        exception.expect(JDODataStoreException.class);
        nationalDefaultLanguageLocationDataService.create(new NationalDefaultLanguage(hi));
    }

}
