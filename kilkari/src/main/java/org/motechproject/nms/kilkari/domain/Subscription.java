package org.motechproject.nms.kilkari.domain;

import org.joda.time.LocalDate;
import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;
import org.motechproject.mds.annotations.Ignore;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.Unique;
import java.util.UUID;

@Entity(tableName = "nms_subscriptions")
public class Subscription {
    @Field
    @Unique
    @Column(length = 36)
    private String subscriptionId;

    @Field
    @Column(allowsNull = "false")
    private Subscriber subscriber;

    @Field
    private SubscriptionPack subscriptionPack;

    @Field
    private SubscriptionStatus status;

    @Field
    private SubscriptionMode mode;

    @Field
    private LocalDate startDate;

    @Field
    private DeactivationReason deactivationReason;

    public Subscription(Subscriber subscriber, SubscriptionPack subscriptionPack, SubscriptionMode mode) {
        this.subscriptionId = UUID.randomUUID().toString();
        this.subscriber = subscriber;
        this.subscriptionPack = subscriptionPack;
        this.mode = mode;
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

    public SubscriptionMode getMode() { return mode; }

    public void setMode(SubscriptionMode mode) { this.mode = mode; }

    public LocalDate getStartDate() { return startDate; }

    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public DeactivationReason getDeactivationReason() { return deactivationReason; }

    public void setDeactivationReason(DeactivationReason deactivationReason) {
        this.deactivationReason = deactivationReason;
    }

    /**
     * Returns the weekId corresponding to today's date.
     * @return
     */
    @Ignore int todaysWeekId() {
        //todo: the work
        return 0;
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
                ", mode=" + mode +
                ", startDate=" + startDate +
                '}';
    }
}
