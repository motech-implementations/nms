package org.motechproject.nms.testing.it.api;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.alerts.contract.AlertService;
import org.motechproject.alerts.contract.AlertsDataService;
import org.motechproject.alerts.domain.AlertStatus;
import org.motechproject.alerts.domain.AlertType;
import org.motechproject.nms.testing.service.TestingService;
import org.motechproject.testing.osgi.BasePaxIT;
import org.motechproject.testing.osgi.container.MotechNativeTestContainerFactory;
import org.motechproject.testing.osgi.http.SimpleHttpClient;
import org.motechproject.testing.utils.TestContext;
import org.ops4j.pax.exam.ExamFactory;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;

import javax.inject.Inject;
import java.io.IOException;

import static org.junit.Assert.assertEquals;


@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@ExamFactory(MotechNativeTestContainerFactory.class)
public class StatusControllerBundleIT extends BasePaxIT {

    private static final String USER = "motech";
    private static final String PASS = "motech";


    @Inject
    TestingService testingService;
    @Inject
    AlertService alertService;
    @Inject
    AlertsDataService alertsDataService;

    @Before
    public void setupTestData() {
        testingService.clearDatabase();
    }

    @Test
    public void verifyNoAlerts() throws InterruptedException, IOException{
        HttpGet httpGet = new HttpGet(String.format("http://localhost:%d/api/status", TestContext.getJettyPort()));
        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(httpGet, USER, PASS);
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        assertEquals("OK", EntityUtils.toString(response.getEntity()));
    }

    @Test
    public void verifyAlerts() throws InterruptedException, IOException{
        alertService.create("foo1", "bar1", "baz1", AlertType.CRITICAL, AlertStatus.NEW, 0, null);
        alertService.create("foo2", "bar2", "baz2", AlertType.HIGH, AlertStatus.READ, 0, null);
        HttpGet httpGet = new HttpGet(String.format("http://localhost:%d/api/status", TestContext.getJettyPort()));
        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(httpGet, USER, PASS);
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        assertEquals("1 NEW ALERT AND 1 READ ALERT", EntityUtils.toString(response.getEntity()));
    }
}
