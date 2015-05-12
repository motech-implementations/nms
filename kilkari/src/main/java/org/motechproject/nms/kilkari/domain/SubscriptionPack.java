package org.motechproject.nms.kilkari.domain;

import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;

import javax.jdo.annotations.Column;
import javax.validation.constraints.NotNull;
import javax.jdo.annotations.Unique;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * Models the kinds of message campaign (i.e. pack) a subscriber can subscribe to, for example antenatal or
 * postnatal
 */
@Entity(tableName = "nms_subscription_packs")
public class SubscriptionPack {

    @Field
    @Unique
    @Column(allowsNull = "false", length = 100)
    @NotNull
    @Size(min = 1, max = 100)
    private String name;

    @Field
    @Column(allowsNull = "false")
    @NotNull
    private SubscriptionPackType type;

    @Field
    @Column(allowsNull = "false")
    @NotNull
    private int weeks;

    @Field
    @Column(allowsNull = "false")
    @NotNull
    private int messagesPerWeek;

    @Field
    private List<SubscriptionPackMessage> weeklyMessages;

    public SubscriptionPack(String name, SubscriptionPackType type, int weeks, int messagesPerWeek,
                            List<SubscriptionPackMessage> weeklyMessages) {
        this.name = name;
        this.type = type;
        this.weeks = weeks;
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

    public int getWeeks() {
        return weeks;
    }

    public void setWeeks(int weeks) {
        this.weeks = weeks;
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
        result = 31 * result + weeks;
        result = 31 * result + messagesPerWeek;
        result = 31 * result + weeklyMessages.hashCode();
        return result;
    }
}
