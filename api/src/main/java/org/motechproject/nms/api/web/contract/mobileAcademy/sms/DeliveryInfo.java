package org.motechproject.nms.api.web.contract.mobileAcademy.sms;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

/**
 * Contains sms delivery info
 */
public class DeliveryInfo {

    @Pattern(message = "Invalid address format", regexp = "tel: \\d{10}$")
    private String address;

    @NotNull
    private DeliveryStatus deliveryStatus;

    public DeliveryInfo() {
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public DeliveryStatus getDeliveryStatus() {
        return deliveryStatus;
    }

    public void setDeliveryStatus(DeliveryStatus deliveryStatus) {
        this.deliveryStatus = deliveryStatus;
    }
}