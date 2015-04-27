package org.motechproject.nms.outbounddialer.osgi;

import org.motechproject.nms.outbounddialer.web.contract.CdrFileNotificationRequest;
import org.motechproject.nms.outbounddialer.web.contract.CdrFileNotificationRequestFileInfo;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.ExamFactory;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;
import org.springframework.beans.factory.annotation.Autowired;
import org.motechproject.testing.osgi.BasePaxIT;
import org.motechproject.testing.osgi.container.MotechNativeTestContainerFactory;
import org.motechproject.testing.osgi.http.SimpleHttpClient;
import org.motechproject.testing.utils.TestContext;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;

import static org.junit.Assert.assertTrue;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@ExamFactory(MotechNativeTestContainerFactory.class)
public class OutboundDialerControllerBundleIT extends BasePaxIT {
    private static final String ADMIN_USERNAME = "motech";
    private static final String ADMIN_PASSWORD = "motech";

    @Test
    public void testCreateCdrFileNotificationRequest() throws IOException, InterruptedException {
        String validTargetFileName = "OBD_NMS1_20150127090000.csv";
        String validCdrSummaryFileName = "CdrSummary_OBD_NMS1_20150127090000.csv";
        String validCdrDetailFileName = "CdrDetail_OBD_NMS1_20150127090000.csv";

        CdrFileNotificationRequestFileInfo cdrSummary =
            new CdrFileNotificationRequestFileInfo(validCdrSummaryFileName, "xxxx", 5000);
        CdrFileNotificationRequestFileInfo cdrDetail =
            new CdrFileNotificationRequestFileInfo(validCdrDetailFileName, "xxxx", 9900);

        CdrFileNotificationRequest cdrFileNotificationRequest =
            new CdrFileNotificationRequest(validTargetFileName, cdrSummary, cdrDetail);

        ObjectMapper mapper = new ObjectMapper();
        String cdrFileNotificationRequestJson = mapper.writeValueAsString(cdrFileNotificationRequest);

        HttpPost httpPost = new HttpPost(String.format("http://localhost:%d/obd/cdrFileNotification",
            TestContext.getJettyPort()));
        httpPost.setHeader("Content-type", "application/json");
        httpPost.setEntity(new StringEntity(cdrFileNotificationRequestJson));

        assertTrue(SimpleHttpClient.execHttpRequest(httpPost, HttpStatus.SC_OK, ADMIN_USERNAME, ADMIN_PASSWORD));
    }


}
