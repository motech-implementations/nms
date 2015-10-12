package org.motechproject.nms.kilkari.service.impl;

import org.datanucleus.store.rdbms.query.ForwardQueryResult;
import org.joda.time.DateTime;
import org.joda.time.Weeks;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.motechproject.event.MotechEvent;
import org.motechproject.event.listener.EventRelay;
import org.motechproject.event.listener.annotations.MotechListener;
import org.motechproject.mds.query.QueryExecution;
import org.motechproject.mds.query.QueryParams;
import org.motechproject.mds.query.SqlQueryExecution;
import org.motechproject.mds.util.InstanceSecurityRestriction;
import org.motechproject.nms.kilkari.domain.DeactivationReason;
import org.motechproject.nms.kilkari.domain.Subscriber;
import org.motechproject.nms.kilkari.domain.Subscription;
import org.motechproject.nms.kilkari.domain.SubscriptionError;
import org.motechproject.nms.kilkari.domain.SubscriptionOrigin;
import org.motechproject.nms.kilkari.domain.SubscriptionPack;
import org.motechproject.nms.kilkari.domain.SubscriptionPackType;
import org.motechproject.nms.kilkari.domain.SubscriptionRejectionReason;
import org.motechproject.nms.kilkari.domain.SubscriptionStatus;
import org.motechproject.nms.kilkari.repository.SubscriberDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionErrorDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionPackDataService;
import org.motechproject.nms.kilkari.service.SubscriptionService;
import org.motechproject.nms.props.domain.DayOfTheWeek;
import org.motechproject.nms.region.domain.Circle;
import org.motechproject.nms.region.domain.Language;
import org.motechproject.scheduler.contract.RepeatingSchedulableJob;
import org.motechproject.scheduler.service.MotechSchedulerService;
import org.motechproject.server.config.SettingsFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
    private static final String SUBSCRIPTION_PURGE_SEC_INTERVAL = "kilkari.purge_closed_subscriptions_sec_interval";
    private static final String WEEKS_TO_KEEP_CLOSED_SUBSCRIPTIONS = "kilkari.weeks_to_keep_closed_subscriptions";

    private static final String SUBSCRIPTION_PURGE_EVENT_SUBJECT = "nms.kilkari.purge_closed_subscriptions";
    private static final String SELECT_SUBSCRIBERS_BY_NUMBER = "select * from nms_subscribers where callingNumber = ?";
    private static final String MORE_THAN_ONE_SUBSCRIBER = "More than one subscriber returned for callingNumber %s";

    private static final String PACK_CACHE_EVICT_MESSAGE = "nms.kilkari.cache.evict.pack";

    //2015-10-21 05:11:56.598
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.S");

    private SettingsFacade settingsFacade;
    private MotechSchedulerService schedulerService;

    private static final int THREE_MONTHS = 90;

    private SubscriberDataService subscriberDataService;
    private SubscriptionPackDataService subscriptionPackDataService;
    private SubscriptionDataService subscriptionDataService;
    private SubscriptionErrorDataService subscriptionErrorDataService;
    private EventRelay eventRelay;

    @Autowired
    public SubscriptionServiceImpl(@Qualifier("kilkariSettings") SettingsFacade settingsFacade,
                                   MotechSchedulerService schedulerService,
                                   SubscriberDataService subscriberDataService,
                                   SubscriptionPackDataService subscriptionPackDataService,
                                   SubscriptionDataService subscriptionDataService,
                                   SubscriptionErrorDataService subscriptionErrorDataService,
                                   EventRelay eventRelay) {
        this.subscriberDataService = subscriberDataService;
        this.subscriptionPackDataService = subscriptionPackDataService;
        this.subscriptionDataService = subscriptionDataService;
        this.subscriptionErrorDataService = subscriptionErrorDataService;
        this.schedulerService = schedulerService;
        this.settingsFacade = settingsFacade;
        this.eventRelay = eventRelay;

        schedulePurgeOfOldSubscriptions();
    }


    public Subscriber getSubscriber(final long callingNumber) {

        SqlQueryExecution<Subscriber> queryExecution = new SqlQueryExecution<Subscriber>() {

            @Override
            public String getSqlQuery() {
                return SELECT_SUBSCRIBERS_BY_NUMBER;
            }

            @Override
            public Subscriber execute(Query query) {
                query.setClass(Subscriber.class);
                ForwardQueryResult fqr = (ForwardQueryResult) query.execute(callingNumber);
                if (fqr.isEmpty()) {
                    return null;
                }
                if (fqr.size() == 1) {
                    return (Subscriber) fqr.get(0);
                }
                throw new IllegalStateException(String.format(MORE_THAN_ONE_SUBSCRIBER, callingNumber));
            }
        };

        return subscriberDataService.executeSQLQuery(queryExecution);

    }

    /**
     * Use the MOTECH scheduler to setup a repeating job
     * The job will start today at the time stored in flw.purge_invalid_flw_start_time in flw.properties
     * It will repeat every flw.purge_invalid_flw_sec_interval seconds (default value is a day)
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

        //Second interval between events
        String intervalProp = settingsFacade.getProperty(SUBSCRIPTION_PURGE_SEC_INTERVAL);
        Integer secInterval = Integer.parseInt(intervalProp);

        LOGGER.debug(String.format("The %s message will be sent every %ss starting at %s",
                SUBSCRIPTION_PURGE_EVENT_SUBJECT, secInterval.toString(), today.toString()));

        //Schedule repeating job
        MotechEvent event = new MotechEvent(SUBSCRIPTION_PURGE_EVENT_SUBJECT);
        RepeatingSchedulableJob job = new RepeatingSchedulableJob(
                event,          //MOTECH event
                null,           //repeatCount, null means infinity
                secInterval,    //repeatIntervalInSeconds
                today.toDate(), //startTime
                null,           //endTime, null means no end time
                true);          //ignorePastFiresAtStart

        schedulerService.safeScheduleRepeatingJob(job);
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
            Subscriber subscriber = getSubscriber(callingNumber);
            purgedSubscriptions++;
            if (subscriber.getSubscriptions().size() == 0) {
                subscriberDataService.delete(subscriber);
                purgedSubscribers++;
            }
        }

        LOGGER.info(String.format("Purged %s subscribers and %s subscriptions with status (%s or %s) and " + "endDate date before %s", purgedSubscribers, purgedSubscriptions, SubscriptionStatus.COMPLETED, SubscriptionStatus.DEACTIVATED, cutoff.toString()));
    }

    @Override
    public void deletePreconditionCheck(Subscription subscription) {
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

    @Override
    public Subscription createSubscription(long callingNumber, Language language,
                                           SubscriptionPack subscriptionPack, SubscriptionOrigin mode) {

        // call overload with null circle
        return createSubscription(callingNumber, language, null, subscriptionPack, mode);
    }

    @Override
    public Subscription createSubscription(long callingNumber, Language language, Circle circle,
                                           SubscriptionPack subscriptionPack, SubscriptionOrigin mode) {

        long number = PhoneNumberHelper.truncateLongNumber(callingNumber);
        Subscriber subscriber = getSubscriber(number);
        Subscription subscription;

        if (subscriber == null) {
            subscriber = new Subscriber(number, language, circle);
            subscriberDataService.create(subscriber);
        }

        // todo: switch to subscriber.getLanguage() & .getCircle() when the ticket is fixed
        // https://applab.atlassian.net/browse/MOTECH-1678
        Language subscriberLanguage = (Language) subscriberDataService.getDetachedField(subscriber, "language");
        Circle subscriberCircle = (Circle) subscriberDataService.getDetachedField(subscriber, "circle");

        if (subscriberLanguage == null && language != null) {
            subscriber.setLanguage(language);
            subscriberDataService.update(subscriber);
        }

        if (subscriberCircle == null && circle != null) {
            subscriber.setCircle(circle);
            subscriberDataService.update(subscriber);
        }

        subscription = (mode == SubscriptionOrigin.IVR) ?
                createSubscriptionViaIvr(subscriber, subscriptionPack) :
                createSubscriptionViaMcts(subscriber, subscriptionPack);

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
                    // however if origin of existing subscription is MCTS then we want to update it to IVR
                    if (existingSubscription.getOrigin() == SubscriptionOrigin.MCTS_IMPORT) {
                        existingSubscription.setOrigin(SubscriptionOrigin.IVR);
                        subscriptionDataService.update(existingSubscription);
                    }

                    return null;
                }
            }
        }

        Subscription subscription = new Subscription(subscriber, pack, SubscriptionOrigin.IVR);
        subscription.setStartDate(DateTime.now().plusDays(1));
        subscription.setStatus(Subscription.getStatus(subscription, DateTime.now()));

        return subscriptionDataService.create(subscription);
    }

    /**
     * There's some rather complex logic for how MCTS subscribers get created and when more than one subscription is
     * allowed for an MSISDN.
     *
     * From #158:
     * MCTS data records with same MSISDN with one having DOB and other having only LMP shall be allowed to subscribe to
     * different packs.
     *
     * From #159:
     * Kilkari Service shall not allow multiple “Active” or “Pending Activation” subscriptions for the same pack on the
     * same MSISDN via bulk upload of MCTS Data or via IVR. In particular, subscription request with a particular
     * MSISDN, shall be rejected IF:
     *  - DOB (with or without LMP) is present in the MCTS record and there is an already “Active” subscription to Child
     *    Pack on this MSISDN
     *  - LMP (without DOB) is present in the MCTS record and there is an already “Active” or “Pending Activation”
     *    subscription to Pregnancy Pack on this MSISDN.
     */
    private Subscription createSubscriptionViaMcts(Subscriber subscriber, SubscriptionPack pack) {
        Subscription subscription;

        if (subscriber.getDateOfBirth() != null && pack.getType() == SubscriptionPackType.CHILD) {
            // DOB (with or without LMP) is present
            if (getActiveSubscription(subscriber, SubscriptionPackType.CHILD) != null)
            {
                // reject the subscription if it already exists
                logRejectedSubscription(subscriber.getCallingNumber(),
                        SubscriptionRejectionReason.ALREADY_SUBSCRIBED, SubscriptionPackType.CHILD);
                return null;
            } else if (Subscription.hasCompletedForStartDate(subscriber.getDateOfBirth(), DateTime.now(), pack)) {
                // TODO: #117 decide whether this case also warrants logging
                return null;
            } else {
                subscription = new Subscription(subscriber, pack, SubscriptionOrigin.MCTS_IMPORT);
                subscription.setStartDate(subscriber.getDateOfBirth());
                subscription.setStatus(Subscription.getStatus(subscription, DateTime.now()));
            }
        } else if (subscriber.getLastMenstrualPeriod() != null && pack.getType() == SubscriptionPackType.PREGNANCY) {
            // LMP is present and DOB is not
            if (getActiveSubscription(subscriber, SubscriptionPackType.PREGNANCY) != null) {
                // reject the subscription if it already exists
                logRejectedSubscription(subscriber.getCallingNumber(),
                        SubscriptionRejectionReason.ALREADY_SUBSCRIBED, SubscriptionPackType.PREGNANCY);
                return null;
            } else if (Subscription.hasCompletedForStartDate(subscriber.getLastMenstrualPeriod().plusDays(THREE_MONTHS),
                            DateTime.now(), pack)) {
                // TODO: #117 decide whether this case also warrants logging
                return null;
            } else {
                // TODO: #160 deal with early subscription
                subscription = new Subscription(subscriber, pack, SubscriptionOrigin.MCTS_IMPORT);

                // the pregnancy pack starts 3 months after LMP
                subscription.setStartDate(subscriber.getLastMenstrualPeriod().plusDays(THREE_MONTHS));
                subscription.setStatus(Subscription.getStatus(subscription, DateTime.now()));
            }
        } else {
            // TODO: #117 need to log any other error cases? In theory we shouldn't land here.
            return null;
        }

        // creating new subscription from MCTS, set welcome flag
        subscription.setNeedsWelcomeMessageViaObd(true);
        return subscriptionDataService.create(subscription);
    }

    private void logRejectedSubscription(long callingNumber, SubscriptionRejectionReason reason,
                                         SubscriptionPackType packType) {
        SubscriptionError error = new SubscriptionError(callingNumber, reason, packType);
        subscriptionErrorDataService.create(error);
    }



    @Override
    public Subscription getActiveSubscription(Subscriber subscriber, SubscriptionPackType type) {
        Iterator<Subscription> subscriptionIterator = subscriber.getSubscriptions().iterator();
        Subscription existingSubscription;

        while (subscriptionIterator.hasNext()) {
            existingSubscription = subscriptionIterator.next();
            if (existingSubscription.getSubscriptionPack().getType() == type) {
                if (type == SubscriptionPackType.PREGNANCY &&
                        (existingSubscription.getStatus() == SubscriptionStatus.ACTIVE ||
                         existingSubscription.getStatus() == SubscriptionStatus.PENDING_ACTIVATION)) {
                    return existingSubscription;
                }
                if (type == SubscriptionPackType.CHILD && existingSubscription.getStatus() == SubscriptionStatus.ACTIVE) {
                    return existingSubscription;
                }
            }
        }
        return null;
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
        subscription.setStatus(Subscription.getStatus(subscription, DateTime.now()));
        subscriptionDataService.update(subscription);
    }

    @Override
    public void activateSubscription(Subscription subscription) {
        if (subscription.getStatus() == SubscriptionStatus.PENDING_ACTIVATION) {
            subscription.setStatus(SubscriptionStatus.ACTIVE);
            subscriptionDataService.update(subscription);
        }
    }

    public void activatePendingSubscriptionsUpTo(final DateTime upToDateTime) {
        SqlQueryExecution sqe = new SqlQueryExecution() {

            private String now = TIME_FORMATTER.print(DateTime.now());

            @Override
            public String getSqlQuery() {
                return String.format("UPDATE nms_subscriptions SET status='ACTIVE', activationDate='%s', " +
                                "modificationDate='%s' WHERE status='PENDING_ACTIVATION' AND startDate < '%s'",
                        now, now, upToDateTime);
            }

            @Override
            public Object execute(Query query) {
                query.execute();
                return null;
            }
        };
        subscriptionDataService.executeSQLQuery(sqe);
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
    @Cacheable(value = "pack", key = "'0-'.concat(#p0)")
    public SubscriptionPack getSubscriptionPack(SubscriptionPackType type) {
        LOGGER.debug("*** NO CACHE getSubscriptionPack(type={}) ***", type);
        return subscriptionPackDataService.byType(type);
    }


    @Override
    @Cacheable(value = "pack", key = "#p0.concat('-0')")
    public SubscriptionPack getSubscriptionPack(String name) {
        LOGGER.debug("*** NO CACHE getSubscriptionPack(name={}) ***", name);
        return subscriptionPackDataService.byName(name);
    }


    @Override
    @Cacheable(value = "packs", key = "-")
    public List<SubscriptionPack> getSubscriptionPacks() {
        //todo: this seems to be called more than once, yet the cache is never evicted, figure out why...
        LOGGER.debug("*** NO CACHE getSubscriptionPacks() ***");
        return subscriptionPackDataService.retrieveAll();
    }


    public Subscription create(Subscription subscription) {
        return subscriptionDataService.create(subscription);
    }


    public List<Subscription> findActiveSubscriptionsForDay(DayOfTheWeek dayOfTheWeek, int page, int pageSize) {
        List<Subscription> subscriptions = new ArrayList<>();
        List<SubscriptionPack> subscriptionPacks = getSubscriptionPacks();

        for (SubscriptionPack subscriptionPack : subscriptionPacks) {

            subscriptions.addAll(findByStatusAndFirstMessageDayOfWeekAndPack(SubscriptionStatus.ACTIVE, dayOfTheWeek,
                    subscriptionPack, page, pageSize));
            if (subscriptionPack.getMessagesPerWeek() == 2) {
                subscriptions.addAll(findByStatusAndSecondMessageDayOfWeekAndPack(SubscriptionStatus.ACTIVE,
                        dayOfTheWeek, subscriptionPack, page, pageSize));
            }
        }

        return subscriptions;
    }

    //todo: IT to make sure chunking works!

    @Override
    public List<Subscription> findByStatusAndFirstMessageDayOfWeekAndPack(final SubscriptionStatus status,
                                                                          final DayOfTheWeek firstMessageDayOfWeek,
                                                                          final SubscriptionPack subscriptionPack,
                                                                          final int page, final int pageSize) {
        @SuppressWarnings("unchecked")
        QueryExecution<List<Subscription>> queryExecution = new QueryExecution<List<Subscription>>() {
            @Override
            public List<Subscription> execute(Query query, InstanceSecurityRestriction restriction) {

                query.setFilter("status == s && firstMessageDayOfWeek == msgDayOfWeek && subscriptionPack == sp");
                query.declareParameters("org.motechproject.nms.kilkari.domain.SubscriptionStatus s, " +
                                        "org.motechproject.nms.props.domain.DayOfTheWeek msgDayOfWeek, " +
                                        "org.motechproject.nms.kilkari.domain.SubscriptionPack sp");
                query.setRange(((page - 1) * pageSize), (page * pageSize) );

                return (List<Subscription>) query.executeWithArray(status, firstMessageDayOfWeek, subscriptionPack);
            }
        };

        return subscriptionDataService.executeQuery(queryExecution);
    }

    //todo: IT to make sure chunking works!

    @Override
    public List<Subscription> findByStatusAndSecondMessageDayOfWeekAndPack(final SubscriptionStatus status,
                                                                           final DayOfTheWeek secondMessageDayOfWeek,
                                                                           final SubscriptionPack subscriptionPack,
                                                                           final int page, final int pageSize) {
        @SuppressWarnings("unchecked")
        QueryExecution<List<Subscription>> queryExecution = new QueryExecution<List<Subscription>>() {
            @Override
            public List<Subscription> execute(Query query, InstanceSecurityRestriction restriction) {

                query.setFilter("status == s && secondMessageDayOfWeek == msgDayOfWeek && subscriptionPack == sp");
                query.declareParameters("org.motechproject.nms.kilkari.domain.SubscriptionStatus s, " +
                        "org.motechproject.nms.props.domain.DayOfTheWeek msgDayOfWeek, " +
                        "org.motechproject.nms.kilkari.domain.SubscriptionPack sp");
                query.setRange(((page - 1) * pageSize), (page * pageSize));

                return (List<Subscription>) query.executeWithArray(status, secondMessageDayOfWeek, subscriptionPack);
            }
        };

        return subscriptionDataService.executeQuery(queryExecution);
    }

    public List<Subscription> findPendingSubscriptionsFromDate(DateTime startDate, int page, int pageSize) {
        return subscriptionDataService.findByStatusAndStartDate(SubscriptionStatus.PENDING_ACTIVATION, startDate, new QueryParams(page, pageSize));
    }

    @CacheEvict(value = {"pack", "packs" }, allEntries = true)
    public void broadcastCacheEvictMessage(SubscriptionPack pack) {
        LOGGER.debug("*** BROADCAST CACHE EVICT MSG ***");
        MotechEvent motechEvent = new MotechEvent(PACK_CACHE_EVICT_MESSAGE);
        eventRelay.sendEventMessage(motechEvent);
    }

    @MotechListener(subjects = { PACK_CACHE_EVICT_MESSAGE })
    @CacheEvict(value = {"pack", "packs" }, allEntries = true)
    public void cacheEvict(MotechEvent event) {
        LOGGER.debug("*** RECEIVE CACHE EVICT MSG ***");
    }
}
