package org.motechproject.nms.kilkari.domain;


public enum SubscriptionRejectionReason {

    ALREADY_SUBSCRIBED,
    INVALID_LOCATION,
    MISSING_DOB,
    MISSING_LMP,
    INVALID_DOB,
    INVALID_LMP,
    MISSING_MSISDN,
    WEEKLY_CALLS_NOT_ANSWERED,
    ACTIVE_CHILD_PRESENT,
    SUBSCRIBER_IS_NOT_PRESENT;
}
