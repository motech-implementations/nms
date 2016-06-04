package org.motechproject.nms.testing.it.api;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.nms.api.web.contract.AddFlwRequest;
import org.motechproject.nms.testing.it.api.utils.RequestBuilder;
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

import static org.junit.Assert.assertTrue;

/**
 * Integration tests for Ops controller
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@ExamFactory(MotechNativeTestContainerFactory.class)
public class OpsControllerBundleIT extends BasePaxIT {

    private String addFlwEndpoint = String.format("http://localhost:%d/api/ops/addFlw",
            TestContext.getJettyPort());
    @Inject
    TestingService testingService;

    @Before
    public void setupTestData() {
        testingService.clearDatabase();
    }

    @Test
    public void testBadAddFlwRequest() throws IOException, InterruptedException {
        HttpPost request = RequestBuilder.createPostRequest(addFlwEndpoint, new AddFlwRequest());
        assertTrue(SimpleHttpClient.execHttpRequest(request, HttpStatus.SC_BAD_REQUEST, RequestBuilder.ADMIN_USERNAME, RequestBuilder.ADMIN_PASSWORD));
    }
}
