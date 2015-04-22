package org.motechproject.nms.api.web.contract.kilkari;

/**
 * InboxSubscriptionDetailResponse
 */
public class InboxSubscriptionDetailResponse {
    private String subscriptionId;
    private String subscriptionPack;
    private String inboxWeekId;
    private String contentFileName;

    public InboxSubscriptionDetailResponse(String subscriptionId, String subscriptionPack, String inboxWeekId,
                                           String contentFileName) {
        this.subscriptionId = subscriptionId;
        this.subscriptionPack = subscriptionPack;
        this.inboxWeekId = inboxWeekId;
        this.contentFileName = contentFileName;
    }

    public String getSubscriptionId() { return subscriptionId; }

    public void setSubscriptionId(String subscriptionId) { this.subscriptionId = subscriptionId; }

    public String getSubscriptionPack() { return subscriptionPack; }

    public void setSubscriptionPack(String subscriptionPack) { this.subscriptionPack = subscriptionPack; }

    public String getInboxWeekId() { return inboxWeekId; }

    public void setInboxWeekId(String inboxWeekId) { this.inboxWeekId = inboxWeekId; }

    public String getContentFileName() { return contentFileName; }

    public void setContentFileName(String contentFileName) { this.contentFileName = contentFileName; }
}
