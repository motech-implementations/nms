package org.motechproject.nms.api.web.contract.mobileAcademy.sms;

/**
 * Contains sms delivery info
 */
public class DeliveryInfo {

    private String address;

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