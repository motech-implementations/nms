package org.motechproject.nms.kilkari.domain;

import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;

/**
 * Entity for logging rejected Kilkari subscriptions.
 */
@Entity
public class SubscriptionError {

    @Field
    private long msisdn;

    @Field
    private SubscriptionRejectionReason rejectionReason;

    @Field
    private SubscriptionPackType packType;

    public SubscriptionError(long msisdn, SubscriptionRejectionReason rejectionReason, SubscriptionPackType packType) {
        this.msisdn = msisdn;
        this.rejectionReason = rejectionReason;
        this.packType = packType;
    }

    public long getMsisdn() {
        return msisdn;
    }

    public void setMsisdn(long msisdn) {
        this.msisdn = msisdn;
    }

    public SubscriptionRejectionReason getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(SubscriptionRejectionReason rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public SubscriptionPackType getPackType() {
        return packType;
    }

    public void setPackType(SubscriptionPackType packType) {
        this.packType = packType;
    }
}
