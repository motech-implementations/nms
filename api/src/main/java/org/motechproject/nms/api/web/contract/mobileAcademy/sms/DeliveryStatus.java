package org.motechproject.nms.api.web.contract.mobileAcademy.sms;

/**
 * Sms delivery status
 */
public enum DeliveryStatus {

    DeliveredToNetwork,
    DeliveredToTerminal,
    DeliveryUncertain,
    DeliveryImpossible,
}