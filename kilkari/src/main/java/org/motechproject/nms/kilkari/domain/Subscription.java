package org.motechproject.nms.kilkari.domain;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.Unique;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@Entity(tableName = "nms_subscriptions")
public class Subscription {

    private static final int DAYS_IN_WEEK = 7;

    @Field
    @Unique
    @Column(allowsNull = "false", length = 36)
    @NotNull
    private String subscriptionId;

    @Field
    @Column(allowsNull = "false")
    @NotNull
    private Subscriber subscriber;

    @Field
    @Column(allowsNull = "false")
    @NotNull
    private SubscriptionPack subscriptionPack;

    @Field
    @Column(allowsNull = "false")
    @NotNull
    private SubscriptionStatus status;

    @Field
    @Column(allowsNull = "false")
    @NotNull
    private SubscriptionOrigin origin;

    @Field
    private DateTime startDate;

    @Field
    private DeactivationReason deactivationReason;

    @Field
    private boolean needsWelcomeMessage;

    public Subscription(Subscriber subscriber, SubscriptionPack subscriptionPack, SubscriptionOrigin origin) {
        this.subscriptionId = UUID.randomUUID().toString();
        this.subscriber = subscriber;
        this.subscriptionPack = subscriptionPack;
        this.origin = origin;
        if (origin == SubscriptionOrigin.MCTS_IMPORT) {
            needsWelcomeMessage = true;
        }
        this.subscriber.getSubscriptions().add(this);
    }

    public String getSubscriptionId() { return subscriptionId; }

    public Subscriber getSubscriber() {
        return subscriber;
    }

    public void setSubscriber(Subscriber subscriber) {
        this.subscriber = subscriber;
    }

    public SubscriptionPack getSubscriptionPack() {
        return subscriptionPack;
    }

    public void setSubscriptionPack(SubscriptionPack subscriptionPack) {
        this.subscriptionPack = subscriptionPack;
    }

    public SubscriptionStatus getStatus() { return status; }

    public void setStatus(SubscriptionStatus status) { this.status = status; }

    public SubscriptionOrigin getOrigin() { return origin; }

    public void setOrigin(SubscriptionOrigin origin) { this.origin = origin; }

    public DateTime getStartDate() { return startDate; }

    public void setStartDate(DateTime startDate) { this.startDate = startDate; }

    public DeactivationReason getDeactivationReason() { return deactivationReason; }

    public void setDeactivationReason(DeactivationReason deactivationReason) {
        this.deactivationReason = deactivationReason;
    }

    public boolean getNeedsWelcomeMessage() {
        return needsWelcomeMessage;
    }

    public void setNeedsWelcomeMessage(boolean needsWelcomeMessage) {
        this.needsWelcomeMessage = needsWelcomeMessage;
    }

    /**
     * Helper method to be called by the OBD process when selecting a message to play for a subscription
     * @param date The date on which the message will be played
     * @return SubscriptionPackMessage with the details of the message to play
     */
    public SubscriptionPackMessage nextScheduledMessage(DateTime date) { //NO CHECKSTYLE CyclomaticComplexity
        if (status != SubscriptionStatus.ACTIVE) {
            throw new IllegalStateException(String.format("Subscription with ID %s is not active", subscriptionId));
        }
        if ((origin == SubscriptionOrigin.MCTS_IMPORT) && needsWelcomeMessage) {
            // Subscriber has been subscribed via MCTS and may not know what Kilkari is; play welcome message this week
            return SubscriptionPackMessage.getWelcomeMessage();
        }

        int daysIntoPack = Days.daysBetween(startDate, date).getDays();
        int messageIndex = -1;
        int currentWeek = daysIntoPack / DAYS_IN_WEEK + 1;
        int daysIntoWeek = daysIntoPack % DAYS_IN_WEEK;

        if (subscriptionPack.getMessagesPerWeek() == 1) {
            if (daysIntoWeek > 0 && daysIntoWeek < 4) {
                // return this week's only message
                messageIndex = currentWeek - 1;
            }
        } else { // messages per week == 2
            if (daysIntoWeek > 0 && daysIntoWeek < 3) {
                // use this week's first message
                messageIndex = 2 * (currentWeek - 1);
            } else if (daysIntoWeek >= 4 && daysIntoWeek < 6) {
                // use this week's second message
                messageIndex = 2 * (currentWeek - 1) + 1;
            }
        }

        if (messageIndex == -1) {
            // there is no message due
            throw new IllegalStateException(
                    String.format("Subscription with ID %s is not due for any scheduled message", subscriptionId));
        }

        return subscriptionPack.getMessages().get(messageIndex);
    }

    /**
     * Helper method to be called by the CDR processor to determine whether to mark subscription status as COMPLETED
     * @param date The date for which subscription status is to be evaluated
     * @return true if the subscription should be marked completed, false otherwise
     */
    public boolean hasCompleted(DateTime date) {
        return hasCompletedForStartDate(startDate, date, subscriptionPack);
    }

    public static boolean hasCompletedForStartDate(DateTime startDate, DateTime today, SubscriptionPack pack) {
        int totalDaysInPack = pack.getWeeks() * DAYS_IN_WEEK;
        int daysSinceStartDate = Days.daysBetween(startDate, today).getDays();

        return totalDaysInPack < daysSinceStartDate;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Subscription that = (Subscription) o;

        return !(subscriptionId != null ? !subscriptionId
                .equals(that.subscriptionId) : that.subscriptionId != null);

    }

    @Override
    public int hashCode() {
        return subscriptionId != null ? subscriptionId.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Subscription{" +
                "subscriptionId='" + subscriptionId + '\'' +
                ", subscriptionPack=" + subscriptionPack +
                ", status=" + status +
                ", origin=" + origin +
                ", startDate=" + startDate +
                '}';
    }
}
