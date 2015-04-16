package org.motechproject.nms.api.web.contract;

import java.util.Set;

/**
 * KilkariResponseInbox
 */
public class KilkariResponseInbox {
    private Set<InboxSubscriptionDetail> inboxSubscriptionDetailList;

    public KilkariResponseInbox(Set<InboxSubscriptionDetail> inboxSubscriptionDetailList) {
        this.inboxSubscriptionDetailList = inboxSubscriptionDetailList;
    }

    public Set<InboxSubscriptionDetail> getInboxSubscriptionDetailList() {
        return inboxSubscriptionDetailList;
    }

    public void setInboxSubscriptionDetailList(Set<InboxSubscriptionDetail> inboxSubscriptionDetailList) {
        this.inboxSubscriptionDetailList = inboxSubscriptionDetailList;
    }
}
