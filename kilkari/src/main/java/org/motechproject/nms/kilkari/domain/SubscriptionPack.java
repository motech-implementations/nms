package org.motechproject.nms.kilkari.domain;

import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;
import org.motechproject.mds.annotations.Ignore;

import java.util.List;

/**
 * Models the kinds of message campaign (i.e. pack) a subscriber can subscribe to, for example antenatal or
 * postnatal
 */
@Entity(tableName = "nms_subscription_packs")
public class SubscriptionPack {

    private static final int NUM_RETRY_FOR_1_MSG_PER_WEEK = 3;
    private static final int NUM_RETRY_FOR_2_MSG_PER_WEEK = 1;

    @Field
    private String name;

    @Field
    private SubscriptionPackType type;

    @Field
    private int messagesPerWeek;

    @Field
    private List<SubscriptionPackMessage> weeklyMessages;

    public SubscriptionPack(String name, SubscriptionPackType type, int messagesPerWeek,
                            List<SubscriptionPackMessage> weeklyMessages) {
        this.name = name;
        this.type = type;
        this.messagesPerWeek = messagesPerWeek;
        this.weeklyMessages = weeklyMessages;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SubscriptionPackType getType() {
        return type;
    }

    public void setType(SubscriptionPackType type) {
        this.type = type;
    }

    public int getMessagesPerWeek() {
        return messagesPerWeek;
    }

    public void setMessagesPerWeek(int messagesPerWeek) {
        if (messagesPerWeek < 1 || messagesPerWeek > 2) {
            throw new IllegalArgumentException(
                    "Subscription packs may not have fewer than one or more than two messages per week.");
        }
        this.messagesPerWeek = messagesPerWeek;
    }

    public List<SubscriptionPackMessage> getWeeklyMessages() {
        return weeklyMessages;
    }

    public void setWeeklyMessages(List<SubscriptionPackMessage> weeklyMessages) {
        this.weeklyMessages = weeklyMessages;
    }

    @Ignore
    public int retryCount() {
        //See SRS 6.2.2 - Figure 7
        if (messagesPerWeek == 1) {
            return NUM_RETRY_FOR_1_MSG_PER_WEEK;
        } else {
            return NUM_RETRY_FOR_2_MSG_PER_WEEK;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SubscriptionPack that = (SubscriptionPack) o;

        return name.equals(that.name);

    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + type.hashCode();
        result = 31 * result + messagesPerWeek;
        result = 31 * result + weeklyMessages.hashCode();
        return result;
    }
}
