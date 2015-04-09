package org.motechproject.nms.api.web.contract;

import java.util.Set;

/**
 *
 */
public class KilkariResponseUserExisting extends KilkariResponseUser {
    private String languageLocationCode;
    private Set<String> subscriptionPackList;

    public KilkariResponseUserExisting(String circle, String languageLocationCode, Set<String> subscriptionPackList) {
        super(circle);
        this.languageLocationCode = languageLocationCode;
        this.subscriptionPackList = subscriptionPackList;
    }

    public String getLanguageLocationCode() {
        return languageLocationCode;
    }

    public void setLanguageLocationCode(String languageLocationCode) {
        this.languageLocationCode = languageLocationCode;
    }

    public Set<String> getSubscriptionPackList() {
        return subscriptionPackList;
    }

    public void setSubscriptionPackList(Set<String> subscriptionPackList) {
        this.subscriptionPackList = subscriptionPackList;
    }
}
