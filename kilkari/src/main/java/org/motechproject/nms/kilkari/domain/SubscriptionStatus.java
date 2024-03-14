package org.motechproject.nms.kilkari.domain;

/*
 * Status of a Kilkari subscription.
 */
public enum SubscriptionStatus {
    ACTIVE,
    PENDING_ACTIVATION,
    DEACTIVATED,
    COMPLETED,
    HOLD,
    PACK_NOT_INITIATED_DUE_TO_ON_HOLD_DATA
}
