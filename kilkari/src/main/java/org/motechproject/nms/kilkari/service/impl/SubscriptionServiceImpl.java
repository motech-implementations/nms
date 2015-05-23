package org.motechproject.nms.kilkari.service.impl;

import org.joda.time.DateTime;
import org.joda.time.Weeks;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.motechproject.event.MotechEvent;
import org.motechproject.event.listener.annotations.MotechListener;
import org.motechproject.mds.query.QueryExecution;
import org.motechproject.mds.query.QueryParams;
import org.motechproject.mds.util.InstanceSecurityRestriction;
import org.motechproject.nms.kilkari.domain.DeactivationReason;
import org.motechproject.nms.kilkari.domain.Subscriber;
import org.motechproject.nms.kilkari.domain.Subscription;
import org.motechproject.nms.kilkari.domain.SubscriptionOrigin;
import org.motechproject.nms.kilkari.domain.SubscriptionPack;
import org.motechproject.nms.kilkari.domain.SubscriptionPackMessage;
import org.motechproject.nms.kilkari.domain.SubscriptionPackType;
import org.motechproject.nms.kilkari.domain.SubscriptionStatus;
import org.motechproject.nms.kilkari.repository.SubscriberDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionPackDataService;
import org.motechproject.nms.kilkari.service.SubscriptionService;
import org.motechproject.nms.props.domain.DayOfTheWeek;
import org.motechproject.nms.region.domain.LanguageLocation;
import org.motechproject.scheduler.contract.RepeatingSchedulableJob;
import org.motechproject.scheduler.service.MotechSchedulerService;
import org.motechproject.server.config.SettingsFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.jdo.Query;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Implementation of the {@link SubscriptionService} interface.
 */
@Service("subscriptionService")
public class SubscriptionServiceImpl implements SubscriptionService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionServiceImpl.class);

    private static final String SUBSCRIPTION_PURGE_TIME = "kilkari.purge_closed_subscriptions_start_time";
    private static final String SUBSCRIPTION_PURGE_MS_INTERVAL = "kilkari.purge_closed_subscriptions_ms_interval";
    private static final String WEEKS_TO_KEEP_CLOSED_SUBSCRIPTIONS = "kilkari.weeks_to_keep_closed_subscriptions";

    private static final String SUBSCRIPTION_PURGE_EVENT_SUBJECT = "nms.kilkari.purge_closed_subscriptions";

    private SettingsFacade settingsFacade;
    private MotechSchedulerService schedulerService;

    private static final int PREGNANCY_PACK_WEEKS = 72;
    private static final int CHILD_PACK_WEEKS = 48;
    private static final int THREE_MONTHS = 90;
    private static final int TWO_MINUTES = 120;
    private static final int TEN_SECS = 10;

    private SubscriberDataService subscriberDataService;
    private SubscriptionPackDataService subscriptionPackDataService;
    private SubscriptionDataService subscriptionDataService;

    @Autowired
    public SubscriptionServiceImpl(@Qualifier("kilkariSettings") SettingsFacade settingsFacade,
                                   MotechSchedulerService schedulerService,
                                   SubscriberDataService subscriberDataService,
                                   SubscriptionPackDataService subscriptionPackDataService,
                                   SubscriptionDataService subscriptionDataService) {
        this.subscriberDataService = subscriberDataService;
        this.subscriptionPackDataService = subscriptionPackDataService;
        this.subscriptionDataService = subscriptionDataService;
        this.schedulerService = schedulerService;
        this.settingsFacade = settingsFacade;

        schedulePurgeOfOldSubscriptions();
        createSubscriptionPacks();
    }


    /**
     * Use the MOTECH scheduler to setup a repeating job
     * The job will start today at the time stored in flw.purge_invalid_flw_start_time in flw.properties
     * It will repeat every flw.purge_invalid_flw_ms_interval milliseconds (default value is a day)
     */
    private void schedulePurgeOfOldSubscriptions() {
        //Calculate today's fire time
        DateTimeFormatter fmt = DateTimeFormat.forPattern("H:m");
        String timeProp = settingsFacade.getProperty(SUBSCRIPTION_PURGE_TIME);
        DateTime time = fmt.parseDateTime(timeProp);
        DateTime today = DateTime.now()                     // This means today's date...
                .withHourOfDay(time.getHourOfDay())         // ...at the hour...
                .withMinuteOfHour(time.getMinuteOfHour())   // ...and minute specified in imi.properties
                .withSecondOfMinute(0)
                .withMillisOfSecond(0);

        //Millisecond interval between events
        String intervalProp = settingsFacade.getProperty(SUBSCRIPTION_PURGE_MS_INTERVAL);
        Long msInterval = Long.parseLong(intervalProp);

        LOGGER.debug(String.format("The %s message will be sent every %sms starting at %s",
                SUBSCRIPTION_PURGE_EVENT_SUBJECT, msInterval.toString(), today.toString()));

        //Schedule repeating job
        MotechEvent event = new MotechEvent(SUBSCRIPTION_PURGE_EVENT_SUBJECT);
        RepeatingSchedulableJob job = new RepeatingSchedulableJob(
                event,          //MOTECH event
                today.toDate(), //startTime
                null,           //endTime, null means no end time
                null,           //repeatCount, null means infinity
                msInterval,     //repeatIntervalInMilliseconds
                true);          //ignorePastFiresAtStart

        schedulerService.safeScheduleRepeatingJob(job);
    }

    /*
     * Create the subscription packs for Kilkari -- a 48-week child pack and a 72-week pregnancy pack. This service
     * method is effectively internal, but made publicly-accessible so that it can be tested in our ITs.
     */
    @Override
    public final void createSubscriptionPacks() {
        // TODO: make this less hard-coded and hacky once we get spec clarification re: how to populate the pack data
        if (subscriptionPackDataService.byName("childPack") == null) {
            createSubscriptionPack("childPack", SubscriptionPackType.CHILD, CHILD_PACK_WEEKS, 1);
        }
        if (subscriptionPackDataService.byName("pregnancyPack") == null) {
            createSubscriptionPack("pregnancyPack", SubscriptionPackType.PREGNANCY, PREGNANCY_PACK_WEEKS, 2);
        }
    }

    @MotechListener(subjects = { SUBSCRIPTION_PURGE_EVENT_SUBJECT })
    public void purgeOldInvalidSubscriptions(MotechEvent event) {
        int weeksToKeepInvalidFLWs = Integer.parseInt(settingsFacade.getProperty(WEEKS_TO_KEEP_CLOSED_SUBSCRIPTIONS));
        final SubscriptionStatus completed = SubscriptionStatus.COMPLETED;
        final SubscriptionStatus deactivated = SubscriptionStatus.DEACTIVATED;
        final DateTime cutoff = DateTime.now().minusWeeks(weeksToKeepInvalidFLWs).withTimeAtStartOfDay();

        @SuppressWarnings("unchecked")
        QueryExecution<List<Subscription>> queryExecution = new QueryExecution<List<Subscription>>() {
            @Override
            public List<Subscription> execute(Query query, InstanceSecurityRestriction restriction) {

                query.setFilter("(status == completed || status == deactivated) && endDate < cutoff");
                query.declareParameters("org.motechproject.nms.kilkari.domain.SubscriptionStatus completed, " +
                                        "org.motechproject.nms.kilkari.domain.SubscriptionStatus deactivated, " +
                                        "org.joda.time.DateTime cutoff");


                return (List<Subscription>) query.execute(completed, deactivated, cutoff);
            }
        };

        List<Subscription> purgeList = subscriptionDataService.executeQuery(queryExecution);

        int purgedSubscribers = 0;
        int purgedSubscriptions = 0;
        for (Subscription subscription : purgeList) {
            Long callingNumber = subscription.getSubscriber().getCallingNumber();

            subscriptionDataService.delete(subscription);

            // I need to load the subscriber since I deleted one of their subscription prior
            Subscriber subscriber = subscriberDataService.findByCallingNumber(callingNumber);
            purgedSubscriptions++;
            if (subscriber.getSubscriptions().size() == 0) {
                subscriberDataService.delete(subscriber);
                purgedSubscribers++;
            }
        }

        LOGGER.info(String.format("Purged %s subscribers and %s subscriptions with status (%s or %s) and " +
                                  "endDate date before %s",
                purgedSubscribers, purgedSubscriptions, SubscriptionStatus.COMPLETED,
                SubscriptionStatus.DEACTIVATED, cutoff.toString()));
    }

    @Override
    public void deleteAllowed(Subscription subscription) {
        int weeksToKeepInvalidFLWs = Integer.parseInt(settingsFacade.getProperty(WEEKS_TO_KEEP_CLOSED_SUBSCRIPTIONS));
        DateTime now = new DateTime();

        if (subscription.getStatus() != SubscriptionStatus.COMPLETED &&
                subscription.getStatus() != SubscriptionStatus.DEACTIVATED) {
            throw new IllegalStateException("Can not delete an open subscription");
        }

        if (subscription.getEndDate() == null) {
            throw new IllegalStateException("Subscription in closed state with null end date");
        }

        if (Math.abs(Weeks.weeksBetween(now, subscription.getEndDate()).getWeeks()) < weeksToKeepInvalidFLWs) {
            throw new IllegalStateException(String.format("Subscription must be closed for %s weeks before deleting",
                                            weeksToKeepInvalidFLWs));
        }
    }


    private void createSubscriptionPack(String name, SubscriptionPackType type, int weeks,
                                                    int messagesPerWeek) {
        List<SubscriptionPackMessage> messages = new ArrayList<>();
        for (int week = 1; week <= weeks; week++) {
            messages.add(new SubscriptionPackMessage(week, String.format("w%s_1", week),
                    String.format("w%s_1.wav", week),
                    TWO_MINUTES - TEN_SECS + (int) (Math.random() * 2 * TEN_SECS)));

            if (messagesPerWeek == 2) {
                messages.add(new SubscriptionPackMessage(week, String.format("w%s_2", week),
                        String.format("w%s_2.wav", week),
                        TWO_MINUTES - TEN_SECS + (int) (Math.random() * 2 * TEN_SECS)));
            }
        }

        subscriptionPackDataService.create(new SubscriptionPack(name, type, weeks, messagesPerWeek, messages));
    }


    @Override
    public Subscription createSubscription(long callingNumber, LanguageLocation languagelocation, SubscriptionPack subscriptionPack,
                                   SubscriptionOrigin mode) {
        Subscriber subscriber = subscriberDataService.findByCallingNumber(callingNumber);
        Subscription subscription;

        if (subscriber == null) {
            subscriber = new Subscriber(callingNumber, languagelocation);
            subscriberDataService.create(subscriber);
        }

        if (subscriber.getLanguageLocation() == null && languagelocation != null) {
            subscriber.setLanguageLocation(languagelocation);
            subscriberDataService.update(subscriber);
        }

        if (mode == SubscriptionOrigin.IVR) {
            subscription = createSubscriptionViaIvr(subscriber, subscriptionPack);
        } else { // MCTS_UPLOAD
            subscription = createSubscriptionViaMcts(subscriber, subscriptionPack);
        }

        if (subscription != null) {
            subscriber.getSubscriptions().add(subscription);
            subscriberDataService.update(subscriber);
        }
        return subscription;
    }

    private Subscription createSubscriptionViaIvr(Subscriber subscriber, SubscriptionPack pack) {
        Iterator<Subscription> subscriptionIterator = subscriber.getSubscriptions().iterator();
        Subscription existingSubscription;

        while (subscriptionIterator.hasNext()) {

            existingSubscription = subscriptionIterator.next();

            if (existingSubscription.getSubscriptionPack().equals(pack)) {
                if (existingSubscription.getStatus().equals(SubscriptionStatus.ACTIVE) ||
                        existingSubscription.getStatus().equals(SubscriptionStatus.PENDING_ACTIVATION)) {
                    // subscriber already has an active subscription to this pack, don't create a new one
                    return null;
                }
            }
        }

        Subscription subscription = new Subscription(subscriber, pack, SubscriptionOrigin.IVR);
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setStartDate(DateTime.now().plusDays(1));

        return subscriptionDataService.create(subscription);
    }

    private Subscription createSubscriptionViaMcts(Subscriber subscriber, SubscriptionPack pack) {
        Subscription subscription;

        if (subscriber.getDateOfBirth() != null && pack.getType() == SubscriptionPackType.CHILD) {
            if (subscriberHasActivePackType(subscriber, SubscriptionPackType.CHILD) ||
                    (Subscription.hasCompletedForStartDate(subscriber.getDateOfBirth(), DateTime.now(), pack))) {

                // TODO: #138 log the rejected subscription
                return null;
            } else {
                subscription = new Subscription(subscriber, pack, SubscriptionOrigin.MCTS_IMPORT);
                subscription.setStartDate(subscriber.getDateOfBirth());
                subscription.setStatus(SubscriptionStatus.ACTIVE);
            }
        } else if (subscriber.getLastMenstrualPeriod() != null && subscriber.getDateOfBirth() == null &&
                pack.getType() == SubscriptionPackType.PREGNANCY) {
            if (subscriberHasActivePackType(subscriber, SubscriptionPackType.PREGNANCY) ||
                    Subscription.hasCompletedForStartDate(subscriber.getLastMenstrualPeriod().plusDays(THREE_MONTHS),
                            DateTime.now(), pack)) {
                // TODO: #138 log the rejected subscription
                return null;
            } else {
                // TODO: #160 deal with early subscription
                subscription = new Subscription(subscriber, pack, SubscriptionOrigin.MCTS_IMPORT);

                // the pregnancy pack starts 3 months after LMP
                subscription.setStartDate(subscriber.getLastMenstrualPeriod().plusDays(THREE_MONTHS));
                subscription.setStatus(SubscriptionStatus.ACTIVE);
            }
        } else {
            // TODO: #138 need to log other error cases?
            return null;
        }

        return subscriptionDataService.create(subscription);
    }

    private boolean subscriberHasActivePackType(Subscriber subscriber, SubscriptionPackType type) {
        Iterator<Subscription> subscriptionIterator = subscriber.getSubscriptions().iterator();
        Subscription existingSubscription;

        while (subscriptionIterator.hasNext()) {
            existingSubscription = subscriptionIterator.next();
            if (existingSubscription.getSubscriptionPack().getType() == type) {
                if (type == SubscriptionPackType.PREGNANCY &&
                        (existingSubscription.getStatus() == SubscriptionStatus.ACTIVE ||
                         existingSubscription.getStatus() == SubscriptionStatus.PENDING_ACTIVATION)) {
                    return true;
                }
                if (type == SubscriptionPackType.CHILD && existingSubscription.getStatus() == SubscriptionStatus.ACTIVE) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public Subscription getSubscription(String subscriptionId) {
        return subscriptionDataService.findBySubscriptionId(subscriptionId);
    }


    @Override
    public void updateStartDate(Subscription subscription, DateTime newReferenceDate) {
        if (subscription.getSubscriptionPack().getType() == SubscriptionPackType.PREGNANCY) {
            subscription.setStartDate(newReferenceDate.plusDays(THREE_MONTHS));
        } else { // CHILD pack
            subscription.setStartDate(newReferenceDate);
        }

        if (Subscription.hasCompletedForStartDate(subscription.getStartDate(), DateTime.now(),
                subscription.getSubscriptionPack())) {
            subscription.setStatus(SubscriptionStatus.COMPLETED);
        }
        subscriptionDataService.update(subscription);
    }

    @Override
    public void deactivateSubscription(Subscription subscription, DeactivationReason reason) {
        if (subscription.getStatus() == SubscriptionStatus.ACTIVE ||
                subscription.getStatus() == SubscriptionStatus.PENDING_ACTIVATION) {
            subscription.setStatus(SubscriptionStatus.DEACTIVATED);
            subscription.setDeactivationReason(reason);
            subscriptionDataService.update(subscription);
        }
        // Else no-op
    }

    @Override
    public SubscriptionPack getSubscriptionPack(String name) {
        return subscriptionPackDataService.byName(name);
    }


    /**
     * To be used by ITs only!
     */
    public void deleteAll() {
        subscriptionDataService.deleteAll();
    }


    public Subscription create(Subscription subscription) {
        return subscriptionDataService.create(subscription);
    }


    public List<Subscription> findActiveSubscriptionsForDay(DayOfTheWeek dayOfTheWeek, int page, int pageSize) {
        return subscriptionDataService.findByStatusAndDay(SubscriptionStatus.ACTIVE, dayOfTheWeek,
                new QueryParams(page, pageSize));
    }


}
