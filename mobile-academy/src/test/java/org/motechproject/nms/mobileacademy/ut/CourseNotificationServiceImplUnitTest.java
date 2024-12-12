package org.motechproject.nms.mobileacademy.ut;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.motechproject.alerts.contract.AlertService;
import org.motechproject.event.MotechEvent;
import org.motechproject.mtraining.service.ActivityService;
import org.motechproject.nms.flw.domain.FrontLineWorker;
import org.motechproject.nms.flw.service.FrontLineWorkerService;
import org.motechproject.nms.imi.service.SmsNotificationService;
import org.motechproject.nms.mobileacademy.domain.CourseCompletionRecord;
import org.motechproject.nms.mobileacademy.repository.CourseCompletionRecordDataService;
import org.motechproject.nms.mobileacademy.service.impl.CourseNotificationServiceImpl;
import org.motechproject.nms.region.domain.District;
import org.motechproject.nms.region.domain.Language;
import org.motechproject.nms.region.domain.State;
import org.motechproject.nms.region.repository.DistrictDataService;
import org.motechproject.scheduler.contract.RepeatingSchedulableJob;
import org.motechproject.scheduler.service.MotechSchedulerService;
import org.motechproject.server.config.SettingsFacade;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CourseNotificationServiceImplUnitTest {


    @Mock
    private FrontLineWorkerService frontLineWorkerService;

    @Mock
    private DistrictDataService districtDataService;

    @Mock
    private SettingsFacade settingsFacade;

    @Mock
    private ActivityService activityService;

    @Mock
    private MotechSchedulerService schedulerService;

    @Mock
    private SmsNotificationService smsNotificationService;

    @Mock
    private AlertService alertService;

    @Mock
    private CourseCompletionRecordDataService courseCompletionRecordDataService;

    @InjectMocks
    private CourseNotificationServiceImpl courseNotificationServiceImpl;

    private static final String SMS_CONTENT_PREFIX = "sms.content.";
    private static final String SMS_TEMPLATE_ID_PREFIX = "sms.templateId.";
    private static final String SMS_MESSAGE_TYPE_PREFIX = "sms.messageType.";
    private static final String SMS_DEFAULT_LANGUAGE_PROPERTY = "default";

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        courseNotificationServiceImpl = new CourseNotificationServiceImpl(
                smsNotificationService,
                settingsFacade,
                activityService,
                schedulerService,
                courseCompletionRecordDataService,
                alertService,
                frontLineWorkerService,
                districtDataService
        );
    }

    @Test
    public void testUpdateSmsStatus_SuccessfulDelivery() {
        // Arrange
        String deliveryStatus = "DELIVERED";
        String callingNumber = "9876543210";
        long flwId = 1L;

        FrontLineWorker flw = mock(FrontLineWorker.class);
        when(flw.getId()).thenReturn(flwId);
        when(frontLineWorkerService.getByContactNumber(Long.parseLong(callingNumber))).thenReturn(flw);

        CourseCompletionRecord ccr = new CourseCompletionRecord(1L,24,"scoresByChapter\":{\"11\":4, \"1\":4, \"2\":4, \"3\":4, \"4\":4, \"5\":4, \"6\":4, \"7\":4, \"8\":4, \"9\":4, \"10\":4}");
        when(courseCompletionRecordDataService.findByFlwId(flwId)).thenReturn(Collections.singletonList(ccr));

        MotechEvent event = mockMotechEvent(callingNumber, deliveryStatus);

        // Act
        courseNotificationServiceImpl.updateSmsStatus(event);

        // Assert
//        verify(courseCompletionRecordDataService).update(ccr);
        assertNull(ccr.getLastDeliveryStatus());
        verifyNoMoreInteractions(schedulerService);
    }

//    @Test
//    public void testUpdateSmsStatus_RetryOnFailure() throws Exception {
//        // Arrange
//        String deliveryStatus = "DELIVERY_IMPOSSIBLE";
//        String callingNumber = "9876543210";
//        long flwId = 1L;
//
//        CourseNotificationServiceImpl courseNotificationServiceMocked = mock(CourseNotificationServiceImpl.class);
//
//        FrontLineWorker flw = mock(FrontLineWorker.class);
//        when(flw.getId()).thenReturn(flwId);
//        when(frontLineWorkerService.getByContactNumber(Long.parseLong(callingNumber))).thenReturn(flw);
//
//        CourseCompletionRecord ccr = new CourseCompletionRecord(1L, 24, "scoresByChapter\":{\"11\":4, \"1\":4, \"2\":4, \"3\":4, \"4\":4, \"5\":4, \"6\":4, \"7\":4, \"8\":4, \"9\":4, \"10\":4}");
//        ccr.setNotificationRetryCount(0);
//        when(courseCompletionRecordDataService.findByFlwId(flwId)).thenReturn(Collections.singletonList(ccr));
//
//        when(settingsFacade.getProperty("sms.retry.count")).thenReturn("3");
//        MotechEvent event = mockMotechEvent(callingNumber, deliveryStatus);
//
//        // Mock SMS params using reflection
//        Map<String, String> smsParams = new HashMap<>();
//        smsParams.put("key", "value");
//
////         Use reflection to access the private method
//        Method buildSmsParamsMethod = CourseNotificationServiceImpl.class.getDeclaredMethod("buildSmsParams", long.class, CourseCompletionRecord.class);
//        buildSmsParamsMethod.setAccessible(true);
//        when(courseNotificationServiceMocked.buildSmsParams(flwId, ccr)).thenReturn(smsParams);
//
//        // Act
//        courseNotificationServiceImpl.updateSmsStatus(event);
//
//        // Assert
//        verify(schedulerService).safeScheduleRepeatingJob(any(RepeatingSchedulableJob.class));
//    }

    @Test
    public void testUpdateSmsStatus_NoCourseCompletionRecord() {
        // Arrange
        String deliveryStatus = "DELIVERED";
        String callingNumber = "9876543210";
        long flwId = 1L;

        FrontLineWorker flw = mock(FrontLineWorker.class);
        when(flw.getId()).thenReturn(flwId);
        when(frontLineWorkerService.getByContactNumber(Long.parseLong(callingNumber))).thenReturn(flw);

        when(courseCompletionRecordDataService.findByFlwId(flwId)).thenReturn(null);

        MotechEvent event = mockMotechEvent(callingNumber, deliveryStatus);

        // Act
        courseNotificationServiceImpl.updateSmsStatus(event);

        // Assert
        verify(courseCompletionRecordDataService, never()).update(any(CourseCompletionRecord.class));
        verify(alertService, never()).create(anyString(), anyString(), anyString(), any(), any(), anyInt(), any());
    }

    @Test
    public void testUpdateSmsStatus_ExceedsRetryLimit() {
        // Arrange
        String deliveryStatus = "DELIVERY_IMPOSSIBLE";
        String callingNumber = "9876543210";
        long flwId = 1L;

        FrontLineWorker flw = mock(FrontLineWorker.class);
        when(flw.getId()).thenReturn(flwId);
        when(frontLineWorkerService.getByContactNumber(Long.parseLong(callingNumber))).thenReturn(flw);

        CourseCompletionRecord ccr = new CourseCompletionRecord(1L,24,"scoresByChapter\":{\"11\":4, \"1\":4, \"2\":4, \"3\":4, \"4\":4, \"5\":4, \"6\":4, \"7\":4, \"8\":4, \"9\":4, \"10\":4}");
        ccr.setNotificationRetryCount(3); // Exceeds limit
        when(courseCompletionRecordDataService.findByFlwId(flwId)).thenReturn(Collections.singletonList(ccr));

        when(settingsFacade.getProperty("sms.retry.count")).thenReturn("3");
        MotechEvent event = mockMotechEvent(callingNumber, deliveryStatus);

        // Act
        courseNotificationServiceImpl.updateSmsStatus(event);

        // Assert
        verify(schedulerService, never()).safeScheduleRepeatingJob(any(RepeatingSchedulableJob.class));
    }

    private MotechEvent mockMotechEvent(String callingNumber, String deliveryStatus) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("address", "sms:" + callingNumber);
        parameters.put("deliveryStatus", deliveryStatus);

        return new MotechEvent("testSubject", parameters);
    }

    @Test
    public void testBuildSmsParams_Success() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Long flwId = 1L;
        CourseCompletionRecord ccr = mock(CourseCompletionRecord.class);
        FrontLineWorker flw = new FrontLineWorker(9876543210L);
        flw.setState(new State("Telangana",123L));

        District district = new District();
        district.setCode(456L);
        district.setName("Hyderabad");
        flw.setDistrict(district);
        flw.setLanguage(new Language());

        when(frontLineWorkerService.getById(flwId)).thenReturn(flw);
        when(settingsFacade.getProperty(SMS_CONTENT_PREFIX + "default")).thenReturn("Sample SMS Content");
        when(settingsFacade.getProperty("sms.entityId.default")).thenReturn("defaultEntityId");
        when(settingsFacade.getProperty("sms.telemarketerId.default")).thenReturn("defaultTelemarketerId");
        when(settingsFacade.getProperty(SMS_TEMPLATE_ID_PREFIX + "default")).thenReturn("template123");
        when(settingsFacade.getProperty(SMS_MESSAGE_TYPE_PREFIX + "default")).thenReturn("transactional");


        Method privateMethod = CourseNotificationServiceImpl.class.getDeclaredMethod("buildSmsParams", Long.class, CourseCompletionRecord.class);
        privateMethod.setAccessible(true);

        Map<String, String> result = (Map<String, String>) privateMethod.invoke(courseNotificationServiceImpl, flwId, ccr);

        assertEquals("Sample SMS Content", result.get("smsContent"));
        assertEquals("defaultEntityId", result.get("smsEntityId"));
        assertEquals("defaultTelemarketerId", result.get("smsTelemarketerId"));
        assertEquals("template123", result.get("smsTemplateId"));
        assertEquals("transactional", result.get("smsMessageType"));

        verify(courseCompletionRecordDataService).update(ccr);
    }

    @Test(expected = InvocationTargetException.class)
    public void testBuildSmsParams_FLWNotFound() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Long flwId = 1L;
        CourseCompletionRecord ccr = mock(CourseCompletionRecord.class);

        when(frontLineWorkerService.getById(flwId)).thenReturn(null);

        Method privateMethod = CourseNotificationServiceImpl.class.getDeclaredMethod("buildSmsParams", Long.class, CourseCompletionRecord.class);
        privateMethod.setAccessible(true);

        Map<String, String> result = (Map<String, String>) privateMethod.invoke(courseNotificationServiceImpl, flwId, ccr);

    }

    @Test
    public void testBuildSmsParams_DefaultLanguageFallback() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Long flwId = 1L;
        CourseCompletionRecord ccr = mock(CourseCompletionRecord.class);
        FrontLineWorker flw = new FrontLineWorker(9876543210L);
        flw.setState(new State("Telangana",123L));

        District district = new District();
        district.setCode(456L);
        district.setName("Hyderabad");
        flw.setDistrict(district);
        flw.setLanguage(new Language());

        when(frontLineWorkerService.getById(flwId)).thenReturn(flw);
        when(settingsFacade.getProperty(SMS_CONTENT_PREFIX + SMS_DEFAULT_LANGUAGE_PROPERTY)).thenReturn("Default SMS Content");
        when(settingsFacade.getProperty("sms.entityId.default")).thenReturn("defaultEntityId");
        when(settingsFacade.getProperty("sms.telemarketerId.default")).thenReturn("defaultTelemarketerId");
        when(settingsFacade.getProperty(SMS_TEMPLATE_ID_PREFIX + SMS_DEFAULT_LANGUAGE_PROPERTY)).thenReturn("defaultTemplateId");
        when(settingsFacade.getProperty(SMS_MESSAGE_TYPE_PREFIX + SMS_DEFAULT_LANGUAGE_PROPERTY)).thenReturn("transactional");

        Method privateMethod = CourseNotificationServiceImpl.class.getDeclaredMethod("buildSmsParams", Long.class, CourseCompletionRecord.class);
        privateMethod.setAccessible(true);
        Map<String, String> result = (Map<String, String>) privateMethod.invoke(courseNotificationServiceImpl, flwId, ccr);

        assertEquals("Default SMS Content", result.get("smsContent"));
        assertEquals("defaultEntityId", result.get("smsEntityId"));
        assertEquals("defaultTelemarketerId", result.get("smsTelemarketerId"));
        assertEquals("defaultTemplateId", result.get("smsTemplateId"));
        assertEquals("transactional", result.get("smsMessageType"));

        verify(courseCompletionRecordDataService).update(ccr);
    }

    @Test(expected = InvocationTargetException.class)
    public void testBuildSmsParams_MissingSmsContent() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Long flwId = 1L;
        CourseCompletionRecord ccr = mock(CourseCompletionRecord.class);
        FrontLineWorker flw = new FrontLineWorker(9876543210L);
        flw.setState(new State("Telangana",123L));

        District district = new District();
        district.setCode(456L);
        district.setName("Hyderabad");
        flw.setDistrict(district);
        flw.setLanguage(new Language());

        when(frontLineWorkerService.getById(flwId)).thenReturn(flw);
        when(settingsFacade.getProperty(SMS_CONTENT_PREFIX + "EN")).thenReturn(null);

        Method privateMethod = CourseNotificationServiceImpl.class.getDeclaredMethod("buildSmsParams", Long.class, CourseCompletionRecord.class);
        privateMethod.setAccessible(true);
        Map<String, String> result = (Map<String, String>) privateMethod.invoke(courseNotificationServiceImpl, flwId, ccr);
    }
}
