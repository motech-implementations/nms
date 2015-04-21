package org.motechproject.nms.api.web.contract.kilkari;

import org.motechproject.nms.api.web.contract.ResponseUser;

import java.util.Set;

/**
 *
 */
public class UserResponse extends ResponseUser {
    private Set<String> subscriptionPackList;

    public UserResponse() {
        super();
    }

    public Set<String> getSubscriptionPackList() {
        return subscriptionPackList;
    }

    public void setSubscriptionPackList(Set<String> subscriptionPackList) {
        this.subscriptionPackList = subscriptionPackList;
    }
}
