package org.motechproject.nms.kilkari.domain;

import org.joda.time.DateTime;
import org.motechproject.mds.annotations.Entity;

/**
 * Data specific to the call contents (ie: what sound file was played) sent by the IVR system as part of
 * {@link InboxCallDetailRecord}
 */
@Entity(tableName = "nms_inbox_call_data")
public class InboxCallData {
    private String subscriptionId;
    private String subscriptionPack;
    private String inboxWeekId;
    private String contentFileName;
    private DateTime startTime;
    private DateTime endTime;

    public InboxCallData() { }

    public InboxCallData(String subscriptionId, String subscriptionPack, String inboxWeekId, String contentFileName,
                         DateTime startTime, DateTime endTime) {
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

    public DateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(DateTime startTime) {
        this.startTime = startTime;
    }

    public DateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(DateTime endTime) {
        this.endTime = endTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        InboxCallData that = (InboxCallData) o;

        if (!subscriptionId.equals(that.subscriptionId)) {
            return false;
        }
        if (!subscriptionPack.equals(that.subscriptionPack)) {
            return false;
        }
        if (!inboxWeekId.equals(that.inboxWeekId)) {
            return false;
        }
        if (!contentFileName.equals(that.contentFileName)) {
            return false;
        }
        if (!startTime.equals(that.startTime)) {
            return false;
        }
        return endTime.equals(that.endTime);

    }

    @Override
    public int hashCode() {
        int result = subscriptionId.hashCode();
        result = 31 * result + subscriptionPack.hashCode();
        result = 31 * result + inboxWeekId.hashCode();
        result = 31 * result + contentFileName.hashCode();
        result = 31 * result + startTime.hashCode();
        result = 31 * result + endTime.hashCode();
        return result;
    }
}
