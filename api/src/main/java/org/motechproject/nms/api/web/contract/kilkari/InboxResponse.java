package org.motechproject.nms.api.web.contract.kilkari;

import java.util.Set;

/**
 * Response body
 *
 * 4.2.2 Get Inbox Details API
 * IVR shall invoke this API to get the Inbox details of the beneficiary, identified by ‘callingNumber’.
 * /api/kilkari/inbox?callingNumber=1111111111&callId=123456789123456&languageLocationCode=10
 *
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

    @Override
    public String toString() {
        return "InboxResponse{" +
                "inboxSubscriptionDetailList=" + inboxSubscriptionDetailList +
                '}';
    }
}
