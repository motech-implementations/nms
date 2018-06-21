package org.motechproject.nms.kilkari.handler;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.motechproject.event.MotechEvent;
import org.motechproject.event.listener.annotations.MotechListener;
import org.motechproject.metrics.service.Timer;
import org.motechproject.nms.kilkari.service.SubscriptionService;
import org.motechproject.nms.kilkari.utils.KilkariConstants;
import org.motechproject.scheduler.contract.CronSchedulableJob;
import org.motechproject.scheduler.service.MotechSchedulerService;
import org.motechproject.server.config.SettingsFacade;
import org.quartz.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Kikari subscription manager that takes care of purging subscriptions and maintaining current cap
 */
@Component
public class SubscriptionManagerHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionManagerHandler.class);

    @Autowired
    @Qualifier("kilkariSettings")
    private SettingsFacade settingsFacade;

    @Autowired
    private MotechSchedulerService schedulerService;

    @Autowired
    private SubscriptionService subscriptionService;

    /**
     * Use the MOTECH scheduler to setup a repeating job
     * The job will start today at the time stored in flw.purge_invalid_flw_start_time in flw.properties
     * It will repeat based on cron (default value is a day @ 4:02am)
     */
    @PostConstruct
    public void initSubscriptionManager() {
        String cronExpression = settingsFacade.getProperty(KilkariConstants.SUBSCRIPTION_MANAGER_CRON);
        if (StringUtils.isBlank(cronExpression)) {
            LOGGER.error("No cron expression found for purging subscriptions");
            return;
        }

        if (!CronExpression.isValidExpression(cronExpression)) {
            String error = "Cron expression from setting is invalid: " + cronExpression;
            LOGGER.error(error);
            throw new IllegalStateException(error);
        }

        CronSchedulableJob subscriptionPurgeJob = new CronSchedulableJob(new MotechEvent(KilkariConstants.SUBSCRIPTION_UPKEEP_SUBJECT), cronExpression);
        schedulerService.safeScheduleJob(subscriptionPurgeJob);
        LOGGER.debug("Scheduled cron job with subject {} and expression {}", KilkariConstants.SUBSCRIPTION_UPKEEP_SUBJECT, cronExpression);
    }

    @MotechListener(subjects = { KilkariConstants.SUBSCRIPTION_UPKEEP_SUBJECT })
    public void upkeepSubscriptions(MotechEvent event) {
        LOGGER.debug("Received event for subscription upkeep");
        DateTime tomorrow = DateTime.now().plusDays(1).withTimeAtStartOfDay();
        Long maxActiveSubscriptions;
        try {
            maxActiveSubscriptions = Long.parseLong(settingsFacade.getProperty(KilkariConstants.SUBSCRIPTION_CAP));
            LOGGER.info("Setting max subscriptions to {}", maxActiveSubscriptions);
        } catch (NumberFormatException nfe) {
            LOGGER.error("***ERROR*** no subscription cap defined, using hardcoded default {}", KilkariConstants.DEFAULT_MAX_ACTIVE_SUBSCRIPTION_CAP);
            maxActiveSubscriptions = KilkariConstants.DEFAULT_MAX_ACTIVE_SUBSCRIPTION_CAP;
        }
        
        subscriptionService.completePastDueSubscriptions();

        Timer timer = new Timer();
        subscriptionService.activatePendingSubscriptionsUpTo(tomorrow, maxActiveSubscriptions);
        LOGGER.debug("Activated all pending subscriptions up to {} in {}", tomorrow, timer.time());

        subscriptionService.toggleMctsSubscriptionCreation(maxActiveSubscriptions);

        // evaluate and activate subscriptions on hold, if there are open slots
        subscriptionService.activateHoldSubscriptions(maxActiveSubscriptions);
    }
}
