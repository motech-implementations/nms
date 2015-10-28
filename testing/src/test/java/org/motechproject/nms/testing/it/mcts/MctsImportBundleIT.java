package org.motechproject.nms.testing.it.mcts;

import org.joda.time.LocalDate;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.commons.date.util.DateUtil;
import org.motechproject.nms.kilkari.domain.MctsMother;
import org.motechproject.nms.kilkari.repository.MctsMotherDataService;
import org.motechproject.nms.mcts.service.MctsWsImportService;
import org.motechproject.nms.region.domain.State;
import org.motechproject.nms.region.repository.StateDataService;
import org.motechproject.nms.testing.it.mcts.util.MockWsHttpServlet;
import org.motechproject.nms.testing.service.TestingService;
import org.motechproject.testing.osgi.BasePaxIT;
import org.motechproject.testing.osgi.container.MotechNativeTestContainerFactory;
import org.motechproject.testing.utils.TestContext;
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
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertNotNull;

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
    private StateDataService stateDataService;

    @Inject
    private TestingService testingService;

    @Before
    public void setUp() throws ServletException, NamespaceException {
        testingService.clearDatabase();
        stateDataService.create(new State("My State", 21L));

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
        List<Long> stateIds = Collections.singletonList(21L);

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(mctsWsImportService.getClass().getClassLoader());
        mctsWsImportService.importFromMcts(stateIds, yesterday, endpoint);
        Thread.currentThread().setContextClassLoader(cl);

        List<MctsMother> mothers = mctsMotherDataService.retrieveAll();
        assertNotNull(mothers);
    }
}

