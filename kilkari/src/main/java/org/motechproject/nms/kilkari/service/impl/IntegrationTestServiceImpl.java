package org.motechproject.nms.kilkari.service.impl;

import org.joda.time.DateTime;
import org.motechproject.nms.kilkari.domain.Subscription;
import org.motechproject.nms.kilkari.domain.SubscriptionStatus;
import org.motechproject.nms.kilkari.repository.CallRetryDataService;
import org.motechproject.nms.kilkari.repository.CallSummaryRecordDataService;
import org.motechproject.nms.kilkari.repository.InboxCallDataDataService;
import org.motechproject.nms.kilkari.repository.InboxCallDetailRecordDataService;
import org.motechproject.nms.kilkari.repository.SubscriberDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionPackDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionPackMessageDataService;
import org.motechproject.nms.kilkari.service.IntegrationTestService;
import org.motechproject.nms.kilkari.service.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("itService")
public class IntegrationTestServiceImpl implements IntegrationTestService {

    @Autowired
    private CallRetryDataService callRetryDataService;
    @Autowired
    private CallSummaryRecordDataService callSummaryRecordDataService;
    @Autowired
    private InboxCallDataDataService inboxCallDataDataService;
    @Autowired
    private InboxCallDetailRecordDataService inboxCallDetailRecordDataService;
    @Autowired
    private SubscriberDataService subscriberDataService;
    @Autowired
    private SubscriptionService subscriptionService;
    @Autowired
    private SubscriptionPackDataService subscriptionPackDataService;
    @Autowired
    private SubscriptionPackMessageDataService subscriptionPackMessageDataService;
    @Autowired
    private SubscriptionDataService subscriptionDataService;


    public void deleteAll() {

        for (Subscription subscription: subscriptionDataService.retrieveAll()) {
            try {
                subscriptionService.deletePreconditionCheck(subscription);
            } catch (IllegalStateException e) {
                subscription.setStatus(SubscriptionStatus.COMPLETED);
                subscription.setEndDate(DateTime.now().minusYears(1));
                subscriptionDataService.update(subscription);
            }
        }

        callRetryDataService.deleteAll();
        callSummaryRecordDataService.deleteAll();
        inboxCallDetailRecordDataService.deleteAll();
        inboxCallDataDataService.deleteAll();
        subscriberDataService.deleteAll();
        subscriptionService.deleteAll();
        subscriptionPackDataService.deleteAll();
        subscriptionPackMessageDataService.deleteAll();
    }
}
