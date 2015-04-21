package org.motechproject.nms.api.web.contract.kilkari;

import org.motechproject.nms.api.web.contract.InboxSubscriptionDetail;

import java.util.Set;

/**
 * InboxResponse
 */
public class InboxResponse {
    private Set<InboxSubscriptionDetail> inboxSubscriptionDetailList;

    public InboxResponse(Set<InboxSubscriptionDetail> inboxSubscriptionDetailList) {
        this.inboxSubscriptionDetailList = inboxSubscriptionDetailList;
    }

    public Set<InboxSubscriptionDetail> getInboxSubscriptionDetailList() {
        return inboxSubscriptionDetailList;
    }

    public void setInboxSubscriptionDetailList(Set<InboxSubscriptionDetail> inboxSubscriptionDetailList) {
        this.inboxSubscriptionDetailList = inboxSubscriptionDetailList;
    }
}
