package org.motechproject.nms.kilkari.domain;

import org.joda.time.LocalDate;
import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;
import org.motechproject.mds.annotations.Ignore;
import org.motechproject.nms.language.domain.Language;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.Unique;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * A Kilkari subscriber (recipient of the service, i.e. a pregnant woman) essentially identified by her
 * phone number
 */
// TODO: Remove maxFetchDepth once https://applab.atlassian.net/browse/MOTECH-1678 is resolved
@Entity(maxFetchDepth = -1, tableName = "nms_subscribers")
public class Subscriber {
    @Field
    @Unique
    @Column(allowsNull = "false")
    private Long callingNumber;

    @Field
    private LocalDate dateOfBirth;

    @Field
    private LocalDate lastMenstrualPeriod;

    @Field
    private Language language;

    @Field
    private String circle;

    //TODO: making this a bi-directional relationship until MOTECH-1638 is fixed. See #31.
    @Field
    @Persistent(mappedBy = "subscriber", defaultFetchGroup = "true")
    private Set<Subscription> subscriptions;

    public Subscriber(Long callingNumber) {
        this.callingNumber = callingNumber;
        this.subscriptions = new HashSet<>();
    }

    public Subscriber(Long callingNumber, Language language) {
        this(callingNumber);
        this.language = language;
    }

    public Subscriber(Long callingNumber, Language language, String circle) {
        this(callingNumber, language);
        this.circle = circle;
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

    @Ignore
    public Set<Subscription> getAllSubscriptions() {
        // TODO: I have no idea why I need to do this, but returning just this.subscriptions always results in
        // an empty set. Bi-directional relationship bug?
        Set<Subscription> allSubscriptions = new HashSet<>();

        Iterator<Subscription> subscriptionIterator = subscriptions.iterator();
        Subscription currentSubscription;
        while (subscriptionIterator.hasNext()) {
            currentSubscription = subscriptionIterator.next();
            allSubscriptions.add(currentSubscription);
        }
        return allSubscriptions;
    }

    public Set<Subscription> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(Set<Subscription> subscriptions) {
        this.subscriptions = subscriptions;
    }

    public String getCircle() {
        return circle;
    }

    public void setCircle(String circle) {
        this.circle = circle;
    }

    @Ignore
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

        return !(callingNumber != null ? !callingNumber.equals(that.callingNumber) : that.callingNumber != null);

    }

    @Override
    public int hashCode() {
        return callingNumber != null ? callingNumber.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Subscriber{" +
                "callingNumber=" + callingNumber +
                ", dateOfBirth=" + dateOfBirth +
                ", lastMenstrualPeriod=" + lastMenstrualPeriod +
                ", circle='" + circle +
                ", subscriptions=" + subscriptions +
                '}';
    }
}
