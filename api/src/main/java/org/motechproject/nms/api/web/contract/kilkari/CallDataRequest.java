package org.motechproject.nms.api.web.contract.kilkari;

/**
 * Request body - inner data structure
 *
 * 4.2.5 Save Inbox Call Details
 * IVR shall invoke this API to send the call detail information corresponding to the Inbox access inbound call
 *    for which inbox message(s) is played.
 * /api/kilkari/inboxCallDetails
 *
 */
public class CallDataRequest {
    private String subscriptionId;
    private String subscriptionPack;
    private String inboxWeekId;
    private String contentFileName;
    private Long startTime;
    private Long endTime;

    public CallDataRequest() { }

    public CallDataRequest(String subscriptionId, String subscriptionPack, String inboxWeekId,
                           String contentFileName, Long startTime, Long endTime) {
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

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }
}
