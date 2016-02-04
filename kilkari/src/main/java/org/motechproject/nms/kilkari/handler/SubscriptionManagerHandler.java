package org.motechproject.nms.kilkari.handler;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.motechproject.event.MotechEvent;
import org.motechproject.event.listener.annotations.MotechListener;
import org.motechproject.metrics.service.Timer;
import org.motechproject.nms.kilkari.service.SubscriptionService;
import org.motechproject.scheduler.contract.CronSchedulableJob;
import org.motechproject.scheduler.service.MotechSchedulerService;
import org.motechproject.server.config.SettingsFacade;
import org.quartz.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;

/**
 * Kikari subscription manager that takes care of purging subscriptions and maintaining current cap
 */
@Component
public class SubscriptionManagerHandler {

    private static final String SUBSCRIPTION_PURGE_EVENT_SUBJECT = "nms.kilkari.purge_closed_subscriptions";
    private static final String SUBSCRIPTION_CAP = "kilkari.subscription.cap";
    private static final String SUBSCRIPTION_MANAGER_CRON = "kilkari.subscription.manager.cron";

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
     * It will repeat every flw.purge_invalid_flw_sec_interval seconds (default value is a day)
     */
    @PostConstruct
    public void initSubscriptionManager() {
        String cronExpression = settingsFacade.getProperty(SUBSCRIPTION_MANAGER_CRON);
        if (StringUtils.isBlank(cronExpression)) {
            LOGGER.error("No cron expression found for purging subscriptions");
            return;
        }

        if (!CronExpression.isValidExpression(cronExpression)) {
            String error = "Cron expression from setting is invalid: " + cronExpression;
            LOGGER.error(error);
            throw new IllegalStateException(error);
        }

        CronSchedulableJob mctsImportJob = new CronSchedulableJob(new MotechEvent(SUBSCRIPTION_PURGE_EVENT_SUBJECT), cronExpression);
        schedulerService.safeScheduleJob(mctsImportJob);
    }

    @MotechListener(subjects = { SUBSCRIPTION_PURGE_EVENT_SUBJECT })
    @Transactional
    public void purgeSubscriptions(MotechEvent event) {
        DateTime tomorrow = DateTime.now().plusDays(1).withTimeAtStartOfDay();
        Long maxActiveSubscriptions = Long.parseLong(settingsFacade.getProperty(SUBSCRIPTION_CAP));

        subscriptionService.purgeOldInvalidSubscriptions();
        subscriptionService.completePastDueSubscriptions();

        Timer timer = new Timer();
        subscriptionService.activatePendingSubscriptionsUpTo(tomorrow);
        LOGGER.debug("Activated all pending subscriptions up to {} in {}", tomorrow, timer.time());

        subscriptionService.toggleMctsSubscriptionCreation(maxActiveSubscriptions);
    }
}
