package org.motechproject.nms.api.web.contract.kilkari;

import java.util.Set;

/**
 * InboxResponse
 */
public class InboxResponse {
    private Set<InboxSubscriptionDetailResponse> inboxSubscriptionDetailList;

    public InboxResponse(Set<InboxSubscriptionDetailResponse> inboxSubscriptionDetailList) {
        this.inboxSubscriptionDetailList = inboxSubscriptionDetailList;
    }

    public Set<InboxSubscriptionDetailResponse> getInboxSubscriptionDetailList() {
        return inboxSubscriptionDetailList;
    }

    public void setInboxSubscriptionDetailList(Set<InboxSubscriptionDetailResponse> inboxSubscriptionDetailList) {
        this.inboxSubscriptionDetailList = inboxSubscriptionDetailList;
    }
}
