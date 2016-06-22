package org.motechproject.nms.api.web.contract.kilkari;

/**
 * Created by sripooja on 20/6/16.
 */
public class DeactivateSubscriptionContract {

    private Long contactNumber;

    public DeactivateSubscriptionContract() {
    }

    public Long getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(Long contactNumber) {
        this.contactNumber = contactNumber;
    }
}
