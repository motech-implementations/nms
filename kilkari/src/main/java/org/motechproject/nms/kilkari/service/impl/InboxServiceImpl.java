package org.motechproject.nms.kilkari.service.impl;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.motechproject.nms.kilkari.domain.InboxCallDetailRecord;
import org.motechproject.nms.kilkari.domain.Subscription;
import org.motechproject.nms.kilkari.domain.SubscriptionOrigin;
import org.motechproject.nms.kilkari.domain.SubscriptionPack;
import org.motechproject.nms.kilkari.domain.SubscriptionPackMessage;
import org.motechproject.nms.kilkari.domain.SubscriptionStatus;
import org.motechproject.nms.kilkari.exception.NoInboxForSubscriptionException;
import org.motechproject.nms.kilkari.repository.InboxCallDetailRecordDataService;
import org.motechproject.nms.kilkari.service.InboxService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementation of the {@link InboxService} interface.
 */
@Service("inboxService")
public class InboxServiceImpl implements InboxService {

    private static final int DAYS_IN_WEEK = 7;

    private InboxCallDetailRecordDataService inboxCallDetailRecordDataService;

    @Autowired
    public InboxServiceImpl(InboxCallDetailRecordDataService inboxCallDetailRecordDataService) {
        this.inboxCallDetailRecordDataService = inboxCallDetailRecordDataService;
    }

    @Override
    public long addInboxCallDetails(InboxCallDetailRecord inboxCallDetailRecord) {
        InboxCallDetailRecord newRecord = inboxCallDetailRecordDataService.create(inboxCallDetailRecord);
        return (long) inboxCallDetailRecordDataService.getDetachedField(newRecord, "id");
    }

    @Override //NO CHECKSTYLE CyclomaticComplexity
    public SubscriptionPackMessage getInboxMessage(Subscription subscription) throws NoInboxForSubscriptionException {
        if ((subscription.getStartDate() == null) || (subscription.getStatus() == SubscriptionStatus.DEACTIVATED)) {
            // there is no inbox for this subscription, throw
            throw new NoInboxForSubscriptionException(String.format("No inbox exists for subscription %s",
                    subscription.getSubscriptionId()));
        }

        if (subscription.getStartDate().isAfter(DateTime.now()) ||
                (subscription.getStatus() == SubscriptionStatus.PENDING_ACTIVATION)) {
            // early subscription, play welcome message
            return null;
        }

        SubscriptionPack pack = subscription.getSubscriptionPack();
        int daysIntoPack = Days.daysBetween(subscription.getStartDate(), DateTime.now()).getDays();
        int messageIndex;
        int currentWeek = daysIntoPack / DAYS_IN_WEEK + 1;
        int daysIntoWeek = daysIntoPack % DAYS_IN_WEEK;

        if (subscription.getStatus() == SubscriptionStatus.COMPLETED) {
            // if > 7 days since subscription completion, return no subscription; otherwise return final message
            if (daysIntoPack > pack.getWeeks() * DAYS_IN_WEEK + DAYS_IN_WEEK) {
                throw new NoInboxForSubscriptionException(String.format("No inbox exists for subscription %s",
                        subscription.getSubscriptionId()));
            }
            currentWeek = pack.getWeeks();
            messageIndex = (pack.getMessagesPerWeek() == 1) ? 1 : 2;
        } else if (subscription.getSubscriptionPack().getMessagesPerWeek() == 1) {

            messageIndex = 1;
        } else {

            // messagesPerWeek == 2
            // day of and next 3 days, so if day of week is Monday: Mon, Tue, Wed, Thu
            // remaining days, so if day of week is Monday: Fri, Sat, Sun
            messageIndex = (daysIntoWeek >= 0 && daysIntoWeek < 4) ? 1 : 2;

        }

        SubscriptionPackMessage spm = subscription.getMessageByWeekAndMessageId(currentWeek, messageIndex);

        if ((subscription.getOrigin() == SubscriptionOrigin.MCTS_IMPORT) &&
                subscription.needsWelcomeMessageViaInbox()) {
            // Subscriber has been subscribed via MCTS and may not know what Kilkari is; play welcome message this week
            spm.setMessageFileName(SubscriptionPackMessage.getWelcomeMessage().getMessageFileName());
        }

        return spm;
    }

}

