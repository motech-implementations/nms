package org.motechproject.nms.kilkari.service;

import org.motechproject.nms.kilkari.domain.InboxCallDetailRecord;
import org.motechproject.nms.kilkari.domain.Subscription;
import org.motechproject.nms.kilkari.domain.SubscriptionPackMessage;
import org.motechproject.nms.kilkari.exception.NoInboxForSubscriptionException;


/**
 *
 */
public interface InboxService {

    long addInboxCallDetails(InboxCallDetailRecord inboxCallDetailRecord);

    SubscriptionPackMessage getInboxMessage(Subscription subscription) throws NoInboxForSubscriptionException;
}
