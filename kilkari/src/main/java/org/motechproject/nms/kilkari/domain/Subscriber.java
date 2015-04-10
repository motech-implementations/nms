package org.motechproject.nms.kilkari.domain;

import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;

import javax.jdo.annotations.Unique;
import java.util.List;

/**
 * A Kilkari subscriber (recipient of the service, ie: a pregnant woman) essentially identified by their phone number
 */
@Entity
public class Subscriber {

    @Field
    @Unique
    private String callingNumber;

    //todo: should this be a Set<> instead of a List<>?
    @Field
    private List<SubscriptionPack> subscriptionPacks;

    public Subscriber() {
    }

    public Subscriber(String callingNumber, List<SubscriptionPack> subscriptionPacks) {
        this.callingNumber = callingNumber;
        this.subscriptionPacks = subscriptionPacks;
    }

    public String getCallingNumber() {
        return callingNumber;
    }

    public void setCallingNumber(String callingNumber) {
        this.callingNumber = callingNumber;
    }

    public List<SubscriptionPack> getSubscriptionPacks() {
        return subscriptionPacks;
    }

    public void setSubscriptionPacks(List<SubscriptionPack> subscriptionPacks) {
        this.subscriptionPacks = subscriptionPacks;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Subscriber that = (Subscriber) o;

        if (!callingNumber.equals(that.callingNumber)) {
            return false;
        }
        return subscriptionPacks.equals(that.subscriptionPacks);

    }

    @Override
    public int hashCode() {
        int result = callingNumber.hashCode();
        result = 31 * result + subscriptionPacks.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Subscriber{" +
                "callingNumber='" + callingNumber + '\'' +
                ", subscriptionPacks=" + subscriptionPacks +
                '}';
    }
}
