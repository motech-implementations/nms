package org.motechproject.nms.kilkari.domain;


public enum SubscriptionRejectionReason {

    ALREADY_SUBSCRIBED,
    MSISDN_ALREADY_SUBSCRIBED,
    BENEFICIARY_ALREADY_SUBSCRIBED,
    INVALID_LOCATION,
    MISSING_DOB,
    MISSING_LMP,
    INVALID_DOB,
    INVALID_LMP,
    MISSING_MSISDN,
    MISSING_MOTHER_ID,
    INVALID_CASE_NO,
    WEEKLY_CALLS_NOT_ANSWERED,
    ABORT_STILLBIRTH_DEATH,
    ACTIVE_CHILD_PRESENT,
    MSISDN_WITH_ONE_ACTIVE_SUBCRIPTION_PRESENT
}
