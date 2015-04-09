package org.motechproject.nms.api.web.contract;

import java.util.Set;

/**
 *
 */
public class KilkariResponseUser extends ResponseUser {
    private Set<String> subscriptionPackList;

    public KilkariResponseUser() {
        super();
    }

    public Set<String> getSubscriptionPackList() {
        return subscriptionPackList;
    }

    public void setSubscriptionPackList(Set<String> subscriptionPackList) {
        this.subscriptionPackList = subscriptionPackList;
    }
}
