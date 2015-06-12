package org.motechproject.nms.kilkari.domain;

import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;

/**
 * Entity for logging rejected Kilkari subscriptions.
 */
@Entity(tableName = "nms_subscription_errors")
public class SubscriptionError {

    @Field
    private long contactNumber;

    @Field
    private SubscriptionRejectionReason rejectionReason;

    @Field
    private SubscriptionPackType packType;

    public SubscriptionError(long contactNumber, SubscriptionRejectionReason rejectionReason, SubscriptionPackType packType) {
        this.contactNumber = contactNumber;
        this.rejectionReason = rejectionReason;
        this.packType = packType;
    }

    public long getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(long contactNumber) {
        this.contactNumber = contactNumber;
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
