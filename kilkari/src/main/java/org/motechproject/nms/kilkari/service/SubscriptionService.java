package org.motechproject.nms.kilkari.service;

import org.joda.time.DateTime;
import org.motechproject.nms.kilkari.domain.DeactivationReason;
import org.motechproject.nms.kilkari.domain.Subscription;
import org.motechproject.nms.kilkari.domain.SubscriptionOrigin;
import org.motechproject.nms.kilkari.domain.SubscriptionPack;
import org.motechproject.nms.props.domain.DayOfTheWeek;
import org.motechproject.nms.region.domain.LanguageLocation;

import java.util.List;

/**
 * Service interface for managing Kilkari subscriptions
 */
public interface SubscriptionService {

    /**
     * Create a new Kilkari subscription. Upon subscription creation, the start date for the subscription will be set
     * based on the subscriber's reference date (DOB or LMP) if it exists. No subscription will be created if the
     * reference date implies that the subscription would already have been completed, or if the beneficiary already
     * has a subscription to the specific pack.
     * @param callingNumber MSISDN of the beneficiary
     * @param languageLocation Language/location pair of the beneficiary
     * @param subscriptionPack The subscription pack (e.g. Pregnancy, Child) for which to subscribe this beneficiary
     * @param mode How the subscription originated -- via IVR or MCTS import
     * @return The created subscription, or null if no subscription was created
     */
    Subscription createSubscription(long callingNumber, LanguageLocation languageLocation, SubscriptionPack subscriptionPack,
                            SubscriptionOrigin mode);

    /**
     * Get the subscription for the specified subscription ID
     * @param subscriptionId ID of the subscription to return, must be a String representation of a UUID
     * @return Subscription with the specified ID, or null if none found
     */
    Subscription getSubscription(String subscriptionId);

    /**
     *
     * @param subscription
     * @param newReferenceDate
     */
    void updateStartDate(Subscription subscription, DateTime newReferenceDate);

    /**
     * Deactivate the specified subscription
     * @param subscription The subscription to deactivate
     * @param reason The reason for deactivation
     */
    void deactivateSubscription(Subscription subscription, DeactivationReason reason);

    /**
     * Get the subscription pack with the specified name
     * @param name The name of the subscription pack to retrieve
     * @return Subscription pack with the specified name, or null if none exists
     */
    SubscriptionPack getSubscriptionPack(String name);

    /**
     * Delete all Kilkari subscriptions. To be used only by test code.
     */
    void deleteAll();

    /**
     * Create the specified subscription in the database. To be used only by test code.
     * @param subscription The subscription to create
     * @return The created subscription
     */
    Subscription create(Subscription subscription);

    /**
     *
     * @param dayOfTheWeek
     * @param page
     * @param pageSize
     * @return
     */
    List<Subscription> findActiveSubscriptionsForDay(DayOfTheWeek dayOfTheWeek, int page, int pageSize);

    /**
     * Generate Pregnancy and Child subscription packs and associated messages. To be used only by test code.
     */
    void createSubscriptionPacks();

}
