package org.motechproject.nms.kilkari.domain;

import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;


/**
 * Models a single message in a Kilkari messaging schedule.
 */

@Entity(tableName = "nms_subscription_pack_messages")
public class SubscriptionPackMessage {

    @Field
    private int week;

    @Field
    private String weekId;

    @Field
    private String messageFileName;

    public SubscriptionPackMessage(int week, String weekId, String messageFileName) {
        this.week = week;
        this.weekId = weekId;
        this.messageFileName = messageFileName;
    }

    public int getWeek() {
        return week;
    }

    public void setWeek(int week) {
        this.week = week;
    }

    public String getWeekId() { return weekId; }

    public void setWeekId(String weekId) { this.weekId = weekId; }

    public String getMessageFileName() {
        return messageFileName;
    }

    public void setMessageFileName(String messageFileName) {
        this.messageFileName = messageFileName;
    }
}
