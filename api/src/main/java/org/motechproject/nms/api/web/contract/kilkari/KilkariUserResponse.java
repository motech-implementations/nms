package org.motechproject.nms.api.web.contract.kilkari;

import org.motechproject.nms.api.web.contract.UserResponse;

import java.util.Set;

/**
 * Response body
 *
 * 4.2.1 Get Subscriber Details API
 * IVR shall invoke this API to get the details of the beneficiary identified by the ‘callingNumber’.
 * /api/kilkari/user?callingNumber=9999999900&operator=A&circle=AP&callId=123456789123456
 *
 */
public class KilkariUserResponse extends UserResponse {
    private Set<String> subscriptionPackList;
    private String circle;

    public KilkariUserResponse() {
        super();
    }

    public Set<String> getSubscriptionPackList() {
        return subscriptionPackList;
    }

    public void setSubscriptionPackList(Set<String> subscriptionPackList) {
        this.subscriptionPackList = subscriptionPackList;
    }

    public String getCircle() {
        return circle;
    }

    public void setCircle(String circle) {
        this.circle = circle;
    }
}
