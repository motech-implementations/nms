package org.motechproject.nms.api.web.contract.mobileAcademy.sms;

/**
 * Sms delivery status container
 */
public class RequestData {

    private DeliveryInfoNotification deliveryInfoNotification;

    public RequestData() {
    }

    public DeliveryInfoNotification getDeliveryInfoNotification() {
        return deliveryInfoNotification;
    }

    public void setDeliveryInfoNotification(DeliveryInfoNotification deliveryInfoNotification) {
        this.deliveryInfoNotification = deliveryInfoNotification;
    }
}