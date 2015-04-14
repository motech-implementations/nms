package org.motechproject.nms.kilkari.domain;

import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;

import javax.jdo.annotations.Unique;

@Entity
public class Subscription {

    @Field
    @Unique
    private String subscriptionId; //this is supposed to be a UUID, not enforcing that yet

    @Field
    private Subscriber subscriber;

    @Field
    private SubscriptionPack subscriptionPack;

    public Subscription(String subscriptionId, Subscriber subscriber, SubscriptionPack subscriptionPack) {
        this.subscriptionId = subscriptionId;
        this.subscriber = subscriber;
        this.subscriptionPack = subscriptionPack;
        this.subscriber.getSubscriptions().add(this);
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Subscription that = (Subscription) o;

        return subscriptionId.equals(that.subscriptionId) && subscriber.equals(this.subscriber) && subscriptionPack.equals(that.subscriptionPack);
    }

    @Override
    public int hashCode() {
        int result = subscriptionId != null ? subscriptionId.hashCode() : 0;
        result = 31 * result + (subscriber != null ? subscriber.hashCode() : 0);
        result = 31 * result + (subscriptionPack != null ? subscriptionPack.hashCode() : 0);
        return result;
    }
}
