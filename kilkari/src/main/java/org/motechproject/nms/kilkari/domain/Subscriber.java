package org.motechproject.nms.kilkari.domain;

import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;
import org.motechproject.nms.language.domain.Language;

import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.Unique;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.joda.time.LocalDate;

/**
 * A Kilkari subscriber (recipient of the service, i.e. a pregnant woman) essentially identified by her
 * phone number
 */
@Entity(tableName = "nms_subscribers")
public class Subscriber {
    public static final int FIELD_SIZE_10 = 10;

    @Field
    @Unique
    private Long callingNumber;

    @Field
    private LocalDate dateOfBirth;

    @Field
    private LocalDate lastMenstrualPeriod;

    @Field
    @Persistent(defaultFetchGroup = "true")
    private Language language;

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

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public LocalDate getLastMenstrualPeriod() {
        return lastMenstrualPeriod;
    }

    public void setLastMenstrualPeriod(LocalDate lastMenstrualPeriod) {
        this.lastMenstrualPeriod = lastMenstrualPeriod;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    public Set<Subscription> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(Set<Subscription> subscriptions) {
        this.subscriptions = subscriptions;
    }

    public Set<Subscription> getActiveSubscriptions() {
        Set<Subscription> activeSubscriptions = new HashSet<>();

        Iterator<Subscription> subscriptionIterator = subscriptions.iterator();
        Subscription currentSubscription;
        while (subscriptionIterator.hasNext()) {
            currentSubscription = subscriptionIterator.next();

            if (currentSubscription.getStatus() == SubscriptionStatus.ACTIVE) {
                activeSubscriptions.add(currentSubscription);
            }
        }
        return activeSubscriptions;
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
