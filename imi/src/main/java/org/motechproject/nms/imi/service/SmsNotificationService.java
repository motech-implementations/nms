package org.motechproject.nms.imi.service;

/**
 * Initiate sms notification with IMI
 */
public interface SmsNotificationService {

    /**
     * Used to initiate sms workflow with IMI
     * @param callingNumber phone number to send sms to
     */
    boolean sendSms(Long callingNumber);
}
