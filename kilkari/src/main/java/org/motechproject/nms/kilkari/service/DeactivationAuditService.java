package org.motechproject.nms.kilkari.service;

import org.motechproject.nms.kilkari.domain.SubscriptionOrigin;
import org.motechproject.nms.kilkari.domain.SubscriptionStatus;

public interface DeactivationAuditService {

    void auditSuccess(String subscriptionId, Long subscriberId, SubscriptionOrigin subscriptionOrigin, SubscriptionStatus status, Long msisdn);
    void auditFailure(String subscriptionId, Long subscriberId, SubscriptionOrigin subscriptionOrigin, SubscriptionStatus status, Long msisdn, String failure);
}

