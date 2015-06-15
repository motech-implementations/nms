package org.motechproject.nms.imi.service.impl;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.joda.time.DateTime;
import org.motechproject.alerts.contract.AlertService;
import org.motechproject.alerts.domain.AlertStatus;
import org.motechproject.alerts.domain.AlertType;
import org.motechproject.nms.imi.service.SmsNotificationService;
import org.motechproject.server.config.SettingsFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * Sms notification service to talk to IMI
 */
@Service("smsNotificationService")
public class SmsNotificationServiceImpl implements SmsNotificationService {

    private static final String SMS_NOTIFICATION_URL = "imi.sms.notification.url";

    private static final String SMS_MESSAGE_CONTENT = "imi.sms.course.completion.message";

    private static final String CALLBACK_URL = "imi.sms.status.callback.url";

    private static final String SMS_SENDER_ID = "imi.sms.sender.id";

    private static final String SMS_TEMPLATE_FILE = "smsTemplate.json";

    private static final Logger LOGGER = LoggerFactory.getLogger(SmsNotificationServiceImpl.class);

    private AlertService alertService;

    private SettingsFacade settingsFacade;

    @Autowired
    public SmsNotificationServiceImpl(AlertService alertService, SettingsFacade settingsFacade) {
        this.alertService = alertService;
        this.settingsFacade = settingsFacade;
    }

    /**
     * Used to initiate sms workflow with IMI
     *
     * @param callingNumber phone number to send sms to
     */
    @Override
    public boolean sendSms(Long callingNumber) {

        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = prepareSmsRequest(callingNumber);

        if (httpPost == null) {
            LOGGER.error("Unable to build POST request for SMS notification");
            return false;
        }

        try {
            HttpResponse response = httpClient.execute(httpPost);
            int responseCode = response.getStatusLine().getStatusCode();
            if (responseCode != HttpStatus.SC_ACCEPTED) {
                String error = String.format("Expecting HTTP 201 response from %s but received HTTP %d : %s ",
                        httpPost.getURI().toString(), responseCode, EntityUtils.toString(response.getEntity()));
                LOGGER.error(error);
                if (response.getEntity() != null && response.getEntity().getContentLength() > 0) {
                    LOGGER.error(getStringFromStream(response.getEntity().getContent()));
                    alertService.create("ResponseCode", "Sms notification",
                            "Could not get expected notification response",
                            AlertType.CRITICAL, AlertStatus.NEW, 0, null);
                }

                return false;
            }
        } catch (IOException ie) {
            LOGGER.error(ie.toString());
            return false;
        }

        return true;
    }

    private HttpPost prepareSmsRequest(Long callingNumber) {

        String senderId = settingsFacade.getProperty(SMS_SENDER_ID);
        String endpoint = settingsFacade.getProperty(SMS_NOTIFICATION_URL);
        String messageContent = settingsFacade.getProperty(SMS_MESSAGE_CONTENT);
        String callbackEndpoint = settingsFacade.getProperty(CALLBACK_URL);

        if (senderId == null || endpoint == null || messageContent == null || callbackEndpoint == null) {

            Map<String, String> alertData = new HashMap<>();
            alertData.put(SMS_SENDER_ID, senderId);
            alertData.put(SMS_NOTIFICATION_URL, endpoint);
            alertData.put(SMS_MESSAGE_CONTENT, messageContent);
            alertData.put(CALLBACK_URL, callbackEndpoint);

            LOGGER.error("Unable to find sms settings. Check IMI sms gateway settings");
            alertService.create("settingsFacade", "properties", "Could not get sms settings",
                    AlertType.CRITICAL,
                    AlertStatus.NEW,
                    0,
                    alertData);
            return null;
        }
        endpoint = endpoint.replace("senderId", senderId);
        HttpPost request = new HttpPost(endpoint);
        request.setHeader("Content-type", "application/json");

        String template = getStringFromStream(settingsFacade.getRawConfig(SMS_TEMPLATE_FILE));
        if (template == null) {
            LOGGER.error("Unable to find sms template. Check IMI sms template file");
            alertService.create("settingsFacade", "template", "Could not get sms template",
                    AlertType.CRITICAL,
                    AlertStatus.NEW,
                    0,
                    null);
            return null;
        }
        template = template.replace("<phoneNumber>", String.valueOf(callingNumber));
        template = template.replace("<senderId>", senderId);
        template = template.replace("<messageContent>", messageContent);
        template = template.replace("<notificationUrl>", callbackEndpoint);
        template = template.replace("<correlationId>", DateTime.now().toString());

        try {
            request.setEntity(new StringEntity(template));
            return request;
        } catch (UnsupportedEncodingException ue) {
            LOGGER.error("Unable to build sms request");
            return null;
        }
    }

    private String getStringFromStream(InputStream inputStream) {
        try {
            StringWriter writer = new StringWriter();
            IOUtils.copy(inputStream, writer, "UTF-8");
            return writer.toString();
        } catch (IOException io) {
            LOGGER.error("Could not get string from stream: " + io.toString());
            return null;
        }
    }
}
