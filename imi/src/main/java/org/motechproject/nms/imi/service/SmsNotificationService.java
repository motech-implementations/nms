package org.motechproject.nms.imi.service;

import java.util.Map;

/**
 * Initiate sms notification with IMI
 */
public interface SmsNotificationService {

    /**
     * Used to initiate sms workflow with IMI
     * @param callingNumber phone number to send sms to
     * @param smsParams sms parameters to send
     */
    boolean sendSms(Long callingNumber, Map<String, String> smsParams);
}
