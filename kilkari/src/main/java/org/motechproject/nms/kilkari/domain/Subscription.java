package org.motechproject.nms.kilkari.domain;

import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;
import org.motechproject.nms.language.domain.Language;
import org.joda.time.LocalDate;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.Unique;
import java.util.UUID;

@Entity(tableName = "nms_subscriptions")
public class Subscription {
    public static final int FIELD_LENGTH_36 = 36;

    @Field
    @Unique
    @Column(length = FIELD_LENGTH_36)
    private String subscriptionId;

    @Field
    private Subscriber subscriber;

    @Field
    private SubscriptionPack subscriptionPack;

    @Field
    private Language language;

    @Field
    private SubscriptionStatus status;

    @Field
    private SubscriptionMode mode;

    @Field
    private LocalDate startDate;

    @Field
    private DeactivationReason deactivationReason;

    public Subscription(Subscriber subscriber, SubscriptionPack subscriptionPack, Language language,
        SubscriptionMode mode) {
        this.subscriptionId = UUID.randomUUID().toString();
        this.subscriber = subscriber;
        this.subscriptionPack = subscriptionPack;
        this.language = language;
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

    public Language getLanguage() { return language; }

    public void setLanguage(Language language) {
        this.language = language;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Subscription that = (Subscription) o;

        return subscriptionId.equals(that.subscriptionId) && subscriber.equals(this.subscriber) &&
                subscriptionPack.equals(that.subscriptionPack) && language.equals(that.getLanguage());
    }

    @Override
    public int hashCode() {
        return subscriptionId.hashCode();
    }
}
