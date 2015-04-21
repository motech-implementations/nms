package org.motechproject.nms.kilkari.domain;

import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;
import org.motechproject.nms.language.domain.Language;

import javax.jdo.annotations.Unique;
import java.util.UUID;

@Entity
public class Subscription {

    @Field
    @Unique
    private String subscriptionId;

    @Field
    private Subscriber subscriber;

    @Field
    private SubscriptionPack subscriptionPack;

    @Field
    private Language language;

    public Subscription(Subscriber subscriber, SubscriptionPack subscriptionPack, Language language) {
        this.subscriptionId = UUID.randomUUID().toString();
        this.subscriber = subscriber;
        this.subscriptionPack = subscriptionPack;
        this.language = language;
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
