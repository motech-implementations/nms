package org.motechproject.nms.testing.it.mcts;

import org.joda.time.LocalDate;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.commons.date.util.DateUtil;
import org.motechproject.event.MotechEvent;
import org.motechproject.nms.flw.domain.FrontLineWorker;
import org.motechproject.nms.flw.repository.FrontLineWorkerDataService;
import org.motechproject.nms.kilkari.domain.MctsChild;
import org.motechproject.nms.kilkari.domain.MctsMother;
import org.motechproject.nms.kilkari.domain.SubscriptionPack;
import org.motechproject.nms.kilkari.domain.SubscriptionPackMessage;
import org.motechproject.nms.kilkari.domain.SubscriptionPackType;
import org.motechproject.nms.kilkari.repository.MctsChildDataService;
import org.motechproject.nms.kilkari.repository.MctsMotherDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionPackDataService;
import org.motechproject.nms.mcts.service.MctsWsImportService;
import org.motechproject.nms.mcts.utils.Constants;
import org.motechproject.nms.region.domain.District;
import org.motechproject.nms.region.domain.HealthBlock;
import org.motechproject.nms.region.domain.HealthFacility;
import org.motechproject.nms.region.domain.HealthFacilityType;
import org.motechproject.nms.region.domain.HealthSubFacility;
import org.motechproject.nms.region.domain.State;
import org.motechproject.nms.region.domain.Taluka;
import org.motechproject.nms.region.repository.DistrictDataService;
import org.motechproject.nms.region.repository.StateDataService;
import org.motechproject.nms.testing.it.mcts.util.MockWsHttpServlet;
import org.motechproject.nms.testing.service.TestingService;
import org.motechproject.testing.osgi.BasePaxIT;
import org.motechproject.testing.osgi.container.MotechNativeTestContainerFactory;
import org.motechproject.testing.utils.TestContext;
import org.motechproject.testing.utils.TimeFaker;
import org.ops4j.pax.exam.ExamFactory;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

import javax.inject.Inject;
import javax.servlet.ServletException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@ExamFactory(MotechNativeTestContainerFactory.class)
public class MctsImportBundleIT extends BasePaxIT {

    @Inject
    private MctsWsImportService mctsWsImportService;

    @Inject
    private HttpService httpService;

    @Inject
    private MctsMotherDataService mctsMotherDataService;

    @Inject
    private MctsChildDataService mctsChildDataService;

    @Inject
    private StateDataService stateDataService;

    @Inject
    private DistrictDataService districtDataService;

    @Inject
    private SubscriptionPackDataService subscriptionPackDataService;

    @Inject
    private FrontLineWorkerDataService flwDataService;

    @Inject
    private TestingService testingService;

    @Before
    public void setUp() throws ServletException, NamespaceException {
        testingService.clearDatabase();
        State state = stateDataService.create(new State("My State", 21L));

        District district = new District();
        district.setCode(4L);
        district.setState(state);
        district.setName("District_Name 4");
        district.setRegionalName("Regional Name 4");

        Taluka taluka = new Taluka();
        taluka.setRegionalName("Taluka Regional");
        taluka.setName("Taluka_Name 1");
        taluka.setCode("0046");
        taluka.setIdentity(55);

        taluka.setDistrict(district);
        district.setTalukas(new ArrayList<>(singletonList(taluka)));

        HealthBlock healthBlock = new HealthBlock();
        healthBlock.setCode(113L);
        healthBlock.setName("HealthBlock_Name 1");
        healthBlock.setRegionalName("HB1");
        healthBlock.setHq("An HQ");

        healthBlock.setTaluka(taluka);
        taluka.setHealthBlocks(new ArrayList<>(singletonList(healthBlock)));

        HealthFacilityType phcType = new HealthFacilityType();
        phcType.setCode(11L);
        phcType.setName("PHC TYPE 111");

        HealthFacility facility = new HealthFacility();
        facility.setName("PHC_NAME 3");
        facility.setRegionalName("Regional PHC 3");
        facility.setCode(111L);
        facility.setHealthFacilityType(phcType);

        healthBlock.setHealthFacilities(new ArrayList<>(singletonList(facility)));
        facility.setHealthBlock(healthBlock);

        HealthSubFacility subcentre = new HealthSubFacility();
        subcentre.setName("SubCentre_Name 1");
        subcentre.setRegionalName("Regional sub name");
        subcentre.setCode(333L);

        subcentre.setHealthFacility(facility);
        facility.setHealthSubFacilities(new ArrayList<>(singletonList(subcentre)));

        districtDataService.create(district);

        SubscriptionPack pregnancyPack = new SubscriptionPack("prg", SubscriptionPackType.PREGNANCY, 10, 10,
                Collections.<SubscriptionPackMessage>emptyList());
        SubscriptionPack childPack  = new SubscriptionPack("child", SubscriptionPackType.CHILD, 5000, 6,
                Collections.<SubscriptionPackMessage>emptyList());

        subscriptionPackDataService.create(pregnancyPack);
        subscriptionPackDataService.create(childPack);

        httpService.registerServlet("/mctsWs", new MockWsHttpServlet(), null, null);

    }

    @After
    public void tearDown() {
        testingService.clearDatabase();
        httpService.unregister("/mctsWs");
    }

    @Test
    public void shouldPerformImport() throws MalformedURLException {
        URL endpoint = new URL(String.format("http://localhost:%d/mctsWs", TestContext.getJettyPort()));
        LocalDate yesterday = DateUtil.today().minusDays(1);
        List<Long> stateIds = singletonList(21L);

        try {
            TimeFaker.fakeToday(DateUtil.newDate(2015, 7, 24));
            // this CL workaround is for an issue with PAX IT logging messing things up
            // shouldn't affect production
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(mctsWsImportService.getClass().getClassLoader());

            // setup motech event
            Map<String, Object> params = new HashMap<>();
            params.put(Constants.DATE_PARAM, yesterday);
            params.put(Constants.STATE_ID_PARAM, 21L);
            params.put(Constants.ENDPOINT_PARAM, endpoint);
            MotechEvent event = new MotechEvent("foobar", params);

            /* Hard to test this since we do async loading now, using test hook. UT already tests message distribution */
            mctsWsImportService.importMothersData(event);
            mctsWsImportService.importChildrenData(event);
            mctsWsImportService.importAnmAshaData(event);
            Thread.currentThread().setContextClassLoader(cl);


            // we expect two of each - the second entry in each ds (3 total) has wrong location data
            List<MctsMother> mothers = mctsMotherDataService.retrieveAll();
            assertEquals(2, mothers.size());

            List<MctsChild> children = mctsChildDataService.retrieveAll();
            assertEquals(2, children.size());

            List<FrontLineWorker> flws = flwDataService.retrieveAll();
            assertEquals(2, flws.size());
        } finally {
            TimeFaker.stopFakingTime();
        }

    }

}

