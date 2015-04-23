package org.motechproject.nms.api.web.contract.kilkari;

/**
 * 4.2.5.1.5 CallData object
 */
public class InboxCallDetailsRequestCallData {
    private String subscriptionId;
    private String subscriptionPack;
    private String inboxWeekId;
    private String contentFileName;
    private String startTime;
    private String endTime;

    public InboxCallDetailsRequestCallData() { }

    public InboxCallDetailsRequestCallData(String subscriptionId, String subscriptionPack, String inboxWeekId, String contentFileName, String startTime, String endTime) {
        this.subscriptionId = subscriptionId;
        this.subscriptionPack = subscriptionPack;
        this.inboxWeekId = inboxWeekId;
        this.contentFileName = contentFileName;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public String getSubscriptionPack() {
        return subscriptionPack;
    }

    public void setSubscriptionPack(String subscriptionPack) {
        this.subscriptionPack = subscriptionPack;
    }

    public String getInboxWeekId() {
        return inboxWeekId;
    }

    public void setInboxWeekId(String inboxWeekId) {
        this.inboxWeekId = inboxWeekId;
    }

    public String getContentFileName() {
        return contentFileName;
    }

    public void setContentFileName(String contentFileName) {
        this.contentFileName = contentFileName;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
}
