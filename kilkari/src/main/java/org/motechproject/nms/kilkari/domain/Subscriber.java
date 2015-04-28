package org.motechproject.nms.kilkari.domain;

import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;

import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.Unique;
import java.util.HashSet;
import java.util.Set;

/**
 * A Kilkari subscriber (recipient of the service, ie: a pregnant woman) essentially identified by their phone number
 */
@Entity(tableName = "nms_subscribers")
public class Subscriber {
    public static final int FIELD_SIZE_10 = 10;

    @Field
    @Unique
    private Long callingNumber;

    //TODO: making this a bi-directional relationship until MOTECH-1638 is fixed. See #31.
    @Field
    @Persistent(mappedBy = "subscriber")
    private Set<Subscription> subscriptions;

    public Subscriber() {
    }

    public Subscriber(Long callingNumber) {
        this.callingNumber = callingNumber;
        this.subscriptions = new HashSet<>();
    }

    public Long getCallingNumber() {
        return callingNumber;
    }

    public void setCallingNumber(Long callingNumber) {
        this.callingNumber = callingNumber;
    }

    public Set<Subscription> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(Set<Subscription> subscriptions) {
        this.subscriptions = subscriptions;
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
        return subscriptions.equals(that.subscriptions);
    }

    @Override
    public int hashCode() {
        return callingNumber.hashCode();
    }

    @Override
    public String toString() {
        return "Subscriber{" +
                "callingNumber='" + callingNumber + '\'' +
                ", subscriptions=" + subscriptions +
                '}';
    }
}
