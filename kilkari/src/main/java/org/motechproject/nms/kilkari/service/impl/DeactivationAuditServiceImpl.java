package org.motechproject.nms.kilkari.service.impl;


import org.motechproject.nms.kilkari.domain.DeactivationSubscriptionRecord;
import org.motechproject.nms.kilkari.domain.SubscriptionOrigin;
import org.motechproject.nms.kilkari.domain.SubscriptionStatus;
import org.motechproject.nms.kilkari.repository.DeactivationSubscriptionRecordDataService;
import org.motechproject.nms.kilkari.service.DeactivationAuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("deactivationAuditService")
public class DeactivationAuditServiceImpl implements DeactivationAuditService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeactivationAuditServiceImpl.class);
    public static final String SUCCESS = "Success";
    public static final String FAILURE = "Failure: ";

    private DeactivationSubscriptionRecordDataService deactivationSubscriptionRecordDataService;

    @Autowired
    public void setDeactivationSubscriptionRecordDataService(DeactivationSubscriptionRecordDataService deactivationSubscriptionRecordDataService) {
        this.deactivationSubscriptionRecordDataService = deactivationSubscriptionRecordDataService;
    }

    private void audit(String subscriptionId, Long subscriberId, SubscriptionOrigin subscriptionOrigin, SubscriptionStatus status, Long msisdn, String outcome) {
        String truncatedOutcome;

        if (outcome.length() > DeactivationSubscriptionRecord.MAX_OUTCOME_LENGTH) {
            truncatedOutcome = outcome.substring(0, DeactivationSubscriptionRecord.MAX_OUTCOME_LENGTH);
            LOGGER.warn("The provided outcome field was more than {} characters and was truncated, original " +
                    "outcome: {}", DeactivationSubscriptionRecord.MAX_OUTCOME_LENGTH - FAILURE.length(), outcome);
        } else {
            truncatedOutcome = outcome;
        }

        deactivationSubscriptionRecordDataService.create(new DeactivationSubscriptionRecord(subscriptionId, subscriberId, subscriptionOrigin, status, msisdn, truncatedOutcome));
    }
    @Override
    public void auditSuccess(String subscriptionId, Long subscriberId, SubscriptionOrigin subscriptionOrigin, SubscriptionStatus status, Long msisdn) {
        audit(subscriptionId, subscriberId, subscriptionOrigin, status, msisdn, SUCCESS);
    }

    @Override
    public void auditFailure(String subscriptionId, Long subscriberId, SubscriptionOrigin subscriptionOrigin, SubscriptionStatus status, Long msisdn, String failure) {
        audit(subscriptionId, subscriberId, subscriptionOrigin, status, msisdn, FAILURE + failure);
    }
}
