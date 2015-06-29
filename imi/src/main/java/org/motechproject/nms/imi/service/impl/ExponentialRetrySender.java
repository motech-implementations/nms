package org.motechproject.nms.imi.service.impl;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.motechproject.alerts.contract.AlertService;
import org.motechproject.alerts.domain.AlertStatus;
import org.motechproject.alerts.domain.AlertType;
import org.motechproject.server.config.SettingsFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.io.IOException;

/**
 * Tries to send the provided HTTP POST to IMI with exponential retries
 */
public class ExponentialRetrySender {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExponentialRetrySender.class);

    private static final String INITIAL_RETRY_DELAY = "imi.initial_retry_delay";
    private static final int INITIAL_RETRY_DELAY_DEFAULT = 2;
    private static final String MAX_NOTIFICATION_RETRY_COUNT = "imi.notification_retry_count";
    private static final int MAX_NOTIFICATION_RETRY_COUNT_DEFAULT = 3;
    public static final int MILLIS_PER_SEC = 1000;

    private SettingsFacade settingsFacade;
    private AlertService alertService;


    @Autowired
    public ExponentialRetrySender(@Qualifier("imiSettings") SettingsFacade settingsFacade,
                                  AlertService alertService) {
        this.settingsFacade = settingsFacade;
        this.alertService = alertService;
    }


    public void sendNotificationRequest(HttpPost httpPost, String id, String name) {
        LOGGER.debug("Sending {}", httpPost);

        int retryDelay;
        try {
            retryDelay = Integer.parseInt(settingsFacade.getProperty(INITIAL_RETRY_DELAY));
        } catch (NumberFormatException e) {
            retryDelay = INITIAL_RETRY_DELAY_DEFAULT;
        }

        int maxRetryCount;
        try {
            maxRetryCount = Integer.parseInt(settingsFacade.getProperty(MAX_NOTIFICATION_RETRY_COUNT));
        } catch (NumberFormatException e) {
            maxRetryCount = MAX_NOTIFICATION_RETRY_COUNT_DEFAULT;
        }
        int count = 0;

        String error = "";

        while (count < maxRetryCount) {
            try {
                HttpClient httpClient = HttpClients.createDefault();
                HttpResponse response = httpClient.execute(httpPost);
                int responseCode = response.getStatusLine().getStatusCode();
                if (responseCode == HttpStatus.SC_ACCEPTED) {
                    return;
                } else {
                    error = String.format("Expecting HTTP 202 response but received HTTP %d: %s",
                            responseCode, EntityUtils.toString(response.getEntity()));
                    LOGGER.warn(error);
                    alertService.create(id, name, error, AlertType.MEDIUM, AlertStatus.NEW, 0, null);
                }
            } catch (IOException e) {
                error = String.format("Unable to send httpPost %s: %s", httpPost.toString(), e.getMessage());
                LOGGER.warn(error);
                alertService.create(id, name, error, AlertType.MEDIUM, AlertStatus.NEW, 0, null);
            }
            count++;

            /**
             * Exponential retry delay
             */
            try {
                Thread.sleep(retryDelay * MILLIS_PER_SEC);
            } catch (InterruptedException e) {
                LOGGER.warn("Thread.sleep interrupted: {}", e.getMessage());
            }
            retryDelay *= retryDelay;
        }

        // Retry count exceeded, consider this a critical error
        LOGGER.error(error);
        alertService.create(id, name, error, AlertType.CRITICAL, AlertStatus.NEW, 0, null);
    }

}
