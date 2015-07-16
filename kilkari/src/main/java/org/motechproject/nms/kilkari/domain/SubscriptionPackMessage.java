package org.motechproject.nms.kilkari.domain;

import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;
import org.motechproject.mds.annotations.Ignore;


/**
 * Models a single message in a Kilkari messaging schedule.
 */

@Entity(tableName = "nms_subscription_pack_messages")
public class SubscriptionPackMessage {

    private static final int TWO_MINUTES = 120;

    @Field
    private String weekId;

    @Field
    private String messageFileName;

    @Field
    private int duration; //In seconds

    public SubscriptionPackMessage(String weekId, String messageFileName, int duration) {
        this.weekId = weekId;
        this.messageFileName = messageFileName;
        this.duration = duration;
    }

    public String getWeekId() { return weekId; }

    public void setWeekId(String weekId) { this.weekId = weekId; }

    public String getMessageFileName() {
        return messageFileName;
    }

    public void setMessageFileName(String messageFileName) {
        this.messageFileName = messageFileName;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    @Ignore
    public static SubscriptionPackMessage getWelcomeMessage() {
        return new SubscriptionPackMessage("w1_1", "w1_1.wav", TWO_MINUTES);
    }
}
