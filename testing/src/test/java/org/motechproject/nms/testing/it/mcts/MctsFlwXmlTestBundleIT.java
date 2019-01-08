package org.motechproject.nms.testing.it.mcts;

import org.joda.time.LocalDate;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.commons.date.util.DateUtil;
import org.motechproject.event.MotechEvent;
import org.motechproject.nms.flw.domain.FlwJobStatus;
import org.motechproject.nms.flw.domain.FrontLineWorker;
import org.motechproject.nms.flw.domain.FrontLineWorkerStatus;
import org.motechproject.nms.flw.repository.FrontLineWorkerDataService;
import org.motechproject.nms.flw.service.FrontLineWorkerService;
import org.motechproject.nms.imi.service.SettingsService;
import org.motechproject.nms.kilkari.domain.*;
import org.motechproject.nms.kilkari.repository.MctsChildDataService;
import org.motechproject.nms.kilkari.repository.MctsMotherDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionPackDataService;
import org.motechproject.nms.mcts.domain.MctsImportAudit;
import org.motechproject.nms.mcts.domain.MctsImportFailRecord;
import org.motechproject.nms.mcts.domain.MctsUserType;
import org.motechproject.nms.mcts.repository.MctsImportAuditDataService;
import org.motechproject.nms.mcts.repository.MctsImportFailRecordDataService;
import org.motechproject.nms.mcts.service.MctsWsImportService;
import org.motechproject.nms.mcts.utils.Constants;
import org.motechproject.nms.region.domain.*;
import org.motechproject.nms.region.repository.DistrictDataService;
import org.motechproject.nms.region.repository.StateDataService;
import org.motechproject.nms.rejectionhandler.domain.FlwImportRejection;
import org.motechproject.nms.rejectionhandler.domain.MotherImportRejection;
import org.motechproject.nms.rejectionhandler.repository.FlwImportRejectionDataService;
import org.motechproject.nms.rejectionhandler.repository.MotherRejectionDataService;
import org.motechproject.nms.testing.it.mcts.util.*;
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
public class MctsFlwXmlTestBundleIT extends BasePaxIT {

    @Inject
    private MctsWsImportService mctsWsImportService;

    @Inject
    private HttpService httpService;

    @Inject
    private MctsMotherDataService mctsMotherDataService;

    @Inject
    private MctsChildDataService mctsChildDataService;

    @Inject
    private MctsImportAuditDataService mctsImportAuditDataService;

    @Inject
    private MctsImportFailRecordDataService mctsImportFailRecordDataService;

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

    @Inject
    private SettingsService settingsService;

    @Inject
    private FlwImportRejectionDataService flwImportRejectionDataService;

    @Inject
    private MotherRejectionDataService motherRejectionDataService;

    @Inject
    private FrontLineWorkerService frontLineWorkerService;

    @Before
    public void setUp() throws ServletException, NamespaceException {
        httpService.registerServlet("/mctsTest", new MockWsHttpServletForTestStateWise(), null, null);
    }

    @After
    public void tearDown() {
        httpService.unregister("/mctsTest");
    }

    @Test
    @Ignore
    public void shouldNotAllowDuplicateASHA() throws MalformedURLException {
        URL endpoint = new URL(String.format("http://localhost:%d/mctsTest", TestContext.getJettyPort()));
        LocalDate lastDateToCheck = DateUtil.today().minusDays(7);
        LocalDate yesterday = DateUtil.today().minusDays(1);

        // this CL workaround is for an issue with PAX IT logging messing things up
        // shouldn't affect production
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(mctsWsImportService.getClass().getClassLoader());

        // setup motech event
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.START_DATE_PARAM, lastDateToCheck);
        params.put(Constants.END_DATE_PARAM, yesterday);
        params.put(Constants.STATE_ID_PARAM, 9L);
        params.put(Constants.ENDPOINT_PARAM, endpoint);
        MotechEvent event = new MotechEvent("foobar", params);
        mctsWsImportService.importAnmAshaData(event);
        Thread.currentThread().setContextClassLoader(cl);

//        Should reject non ASHA FLWs
        List<MctsImportAudit> mctsImportAudits = mctsImportAuditDataService.retrieveAll();
        assertEquals(1, mctsImportAudits.get(0).getAccepted());
        assertEquals(3, mctsImportAudits.get(0).getRejected());
        assertEquals(lastDateToCheck, mctsImportAudits.get(0).getStartImportDate());
        assertEquals(yesterday, mctsImportAudits.get(0).getEndImportDate());

        List<FrontLineWorker> flws = flwDataService.retrieveAll();
        assertEquals(1, flws.size());
        List<FlwImportRejection> flwImportRejections = flwImportRejectionDataService.retrieveAll();
        assertEquals(1, flwImportRejections.size());
        assertEquals(RejectionReasons.MOBILE_NUMBER_ALREADY_IN_USE.toString(), flwImportRejections.get(0).getRejectionReason());
    }


}

