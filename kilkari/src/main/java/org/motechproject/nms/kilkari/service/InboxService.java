package org.motechproject.nms.kilkari.service;

import org.motechproject.nms.kilkari.domain.InboxCallDetailRecord;
import org.motechproject.nms.kilkari.domain.Subscription;
import org.motechproject.nms.kilkari.domain.SubscriptionPackMessage;
import org.motechproject.nms.kilkari.exception.NoInboxForSubscriptionException;


/**
 * Service interface for handling the Kilkar inbox.
 */
public interface InboxService {

    /**
     *
     * @param inboxCallDetailRecord
     * @return
     */
    long addInboxCallDetails(InboxCallDetailRecord inboxCallDetailRecord);

    /**
     *
     * @param subscription
     * @return
     * @throws NoInboxForSubscriptionException
     */
    SubscriptionPackMessage getInboxMessage(Subscription subscription) throws NoInboxForSubscriptionException;
}
