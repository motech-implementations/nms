package org.motechproject.nms.imi.ut;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.motechproject.alerts.contract.AlertService;
import org.motechproject.alerts.domain.AlertStatus;
import org.motechproject.alerts.domain.AlertType;
import org.motechproject.nms.imi.service.SmsNotificationService;
import org.motechproject.nms.imi.service.impl.SmsNotificationServiceImpl;
import org.motechproject.server.config.SettingsFacade;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

public class SmsNotificationServiceUnitTest {
    @Mock
    private SettingsFacade settingsFacade;

    @Mock
    private AlertService alertService;

    private SmsNotificationServiceImpl smsService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        smsService = new SmsNotificationServiceImpl(alertService, settingsFacade);
    }

    @Test
    public void testPrepareSmsRequest_Success() throws Exception {
        // Arrange
        Long callingNumber = 1234567890L;
        Map<String, String> smsParams = new HashMap<>();
        smsParams.put("smsContent", "Test SMS Content");
        smsParams.put("smsEntityId", "entity123");
        smsParams.put("smsTelemarketerId", "telemarketer123");
        smsParams.put("smsTemplateId", "template123");
        smsParams.put("smsMessageType", "Transactional");

        when(settingsFacade.getProperty("imi.sms.sender.id")).thenReturn("Sender123");
        when(settingsFacade.getProperty("imi.sms.notification.url")).thenReturn("http://example.com/endpoint/senderId");
        when(settingsFacade.getProperty("imi.sms.status.callback.url")).thenReturn("http://callback.url");
        when(settingsFacade.getProperty("imi.sms.authentication.key")).thenReturn("auth-key-123");
        when(settingsFacade.getRawConfig("smsTemplate.json")).thenReturn(new ByteArrayInputStream("{\n    \"phoneNumber\": \"<phoneNumber>\",\n    \"senderId\": \"<senderId>\",\n    \"messageContent\": \"<messageContent>\",\n    \"smsTemplateId\": \"<smsTemplateId>\",\n    \"smsEntityId\": \"<smsEntityId>\",\n    \"smsTelemarketerId\": \"<smsTelemarketerId>\",\n    \"notificationUrl\": \"<notificationUrl>\",\n    \"correlationId\": \"<correlationId>\",\n    \"messageType\": \"<messageType>\"\n}".getBytes()));

        // Act
        HttpPost request = smsService.prepareSmsRequest(callingNumber, smsParams);

        // Assert
        assertNotNull(request);
        assertEquals("http://example.com/endpoint/Sender123", request.getURI().toString());
        assertEquals("application/json", request.getFirstHeader("Content-type").getValue());
        assertEquals("auth-key-123", request.getFirstHeader("Key").getValue());
        StringEntity entity = (StringEntity) request.getEntity();
        String entityContent = entityToString(entity);
        assertTrue(entityContent.contains("1234567890"));
        assertTrue(entityContent.contains("Sender123"));
        assertTrue(entityContent.contains("Test SMS Content"));
        assertTrue(entityContent.contains("entity123"));
        assertTrue(entityContent.contains("template123"));
        assertTrue(entityContent.contains("telemarketer123"));
        assertTrue(entityContent.contains("Transactional"));
    }

    @Test
    public void testPrepareSmsRequest_MissingSettings() {
        // Arrange
        Long callingNumber = 1234567890L;
        Map<String, String> smsParams = new HashMap<>();
        smsParams.put("smsContent", "Test SMS Content");
        smsParams.put("smsEntityId", "entity123");
        smsParams.put("smsTelemarketerId", "telemarketer123");
        smsParams.put("smsTemplateId", "template123");
        smsParams.put("smsMessageType", "Transactional");

        when(settingsFacade.getProperty("SMS_SENDER_ID")).thenReturn(null);

        // Act
        HttpPost request = smsService.prepareSmsRequest(callingNumber, smsParams);

        // Assert
        assertNull(request);
        verify(alertService).create(
                eq("settingsFacade"),
                eq("properties"),
                eq("Could not get sms settings"),
                eq(AlertType.CRITICAL),
                eq(AlertStatus.NEW),
                eq(0),
                anyMap()
        );
    }

    // Utility method to convert StringEntity to String
    private String entityToString(StringEntity entity) throws Exception {
        java.io.InputStream inputStream = entity.getContent();
        java.util.Scanner s = new java.util.Scanner(inputStream).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
}
