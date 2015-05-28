package org.motechproject.nms.api.web.contract.mobileAcademy.sms;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * Sms delivery status container
 */
public class RequestData {

    @Valid
    @NotNull
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