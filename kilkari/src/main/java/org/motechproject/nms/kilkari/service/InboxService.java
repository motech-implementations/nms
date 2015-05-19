package org.motechproject.nms.kilkari.service;

import org.motechproject.nms.kilkari.domain.InboxCallDetailRecord;
import org.motechproject.nms.kilkari.domain.Subscription;
import org.motechproject.nms.kilkari.domain.SubscriptionPackMessage;
import org.motechproject.nms.kilkari.exception.NoInboxForSubscriptionException;


/**
 * Service interface for handling the Kilkari inbox.
 */
public interface InboxService {

    /**
     * Records details about an incoming call to the Kilkari Inbox.
     * @param inboxCallDetailRecord Containing information sent to MOTECH by the IVR system when a Kilkari subscriber
     *                              calls her Inbox
     * @return The ID of the new record created in the database
     */
    long addInboxCallDetails(InboxCallDetailRecord inboxCallDetailRecord);

    /**
     * Gets the current Inbox message for the specified subscription.
     * @param subscription The subscription for which to get the current Inbox message
     * @return SubscriptionPackMessage that should be played when the beneficiary calls her Inbox
     * @throws NoInboxForSubscriptionException If the subscription exists but is inactive
     */
    SubscriptionPackMessage getInboxMessage(Subscription subscription) throws NoInboxForSubscriptionException;
}
