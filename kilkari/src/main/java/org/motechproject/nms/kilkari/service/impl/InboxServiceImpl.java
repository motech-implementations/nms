package org.motechproject.nms.kilkari.service.impl;

import org.joda.time.DateTime;
import org.motechproject.nms.kilkari.domain.InboxCallDetailRecord;
import org.motechproject.nms.kilkari.domain.Subscription;
import org.motechproject.nms.kilkari.domain.SubscriptionPackMessage;
import org.motechproject.nms.kilkari.domain.SubscriptionStatus;
import org.motechproject.nms.kilkari.exception.NoInboxForSubscriptionException;
import org.motechproject.nms.kilkari.repository.InboxCallDetailRecordDataService;
import org.motechproject.nms.kilkari.service.InboxService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementation of the {@link InboxService} interface.
 */
@Service("inboxService")
public class InboxServiceImpl implements InboxService {

    private static final Logger LOGGER = LoggerFactory.getLogger(InboxServiceImpl.class);

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
            LOGGER.debug(String.format("Subscription is null or deactivated: %s", subscription.getSubscriptionId()));
            throw new NoInboxForSubscriptionException(String.format("No inbox exists for subscription %s",
                    subscription.getSubscriptionId()));
        }

        if (subscription.getStartDate().isAfter(DateTime.now()) ||
                (subscription.getStatus() == SubscriptionStatus.PENDING_ACTIVATION)) {
            // early subscription, play welcome message
            return null;
        }

        try {
            return subscription.nextScheduledMessage(DateTime.now());
        } catch (IllegalStateException ise) {
            String exceptionMessage = String.format(
                    "Unable to get subscription pack message for subscription %s. %s",
                    subscription.getSubscriptionId(), ise.toString());
            LOGGER.debug(exceptionMessage);
            throw new NoInboxForSubscriptionException(ise, exceptionMessage);
        }
    }

}

