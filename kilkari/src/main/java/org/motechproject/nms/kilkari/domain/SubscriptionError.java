package org.motechproject.nms.kilkari.domain;

import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;

import javax.validation.constraints.NotNull;

/**
 * Entity for logging rejected Kilkari subscriptions.
 */
@Entity(tableName = "nms_subscription_errors")
public class SubscriptionError {

    @Field
    private long contactNumber;

    @Field
    private String beneficiaryId;   // MctsId or RchId based on importOrigin

    @Field
    private SubscriptionRejectionReason rejectionReason;

    @Field
    private SubscriptionPackType packType;

    @Field
    private String rejectionMessage;

    @Field
    @NotNull
    private SubscriptionOrigin importOrigin;

    public SubscriptionError(long contactNumber, SubscriptionRejectionReason rejectionReason,
                             SubscriptionPackType packType, SubscriptionOrigin importOrigin) {
        this(contactNumber, rejectionReason, packType, null, importOrigin);
    }

    public SubscriptionError(long contactNumber, SubscriptionRejectionReason rejectionReason,
                             SubscriptionPackType packType, String rejectionMessage, SubscriptionOrigin importOrigin) {
        this.contactNumber = contactNumber;
        this.rejectionReason = rejectionReason;
        this.packType = packType;
        this.rejectionMessage = rejectionMessage;
        this.importOrigin = importOrigin;
    }

    public SubscriptionError(long contactNumber, String beneficiaryId, SubscriptionRejectionReason rejectionReason,
                             SubscriptionPackType packType, String rejectionMessage, SubscriptionOrigin importOrigin) {
        this.contactNumber = contactNumber;
        this.beneficiaryId = beneficiaryId;
        this.rejectionReason = rejectionReason;
        this.packType = packType;
        this.rejectionMessage = rejectionMessage;
        this.importOrigin = importOrigin;
    }

    public long getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(long contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getBeneficiaryId() {
        return beneficiaryId;
    }

    public void setBeneficiaryId(String beneficiaryId) {
        this.beneficiaryId = beneficiaryId;
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

    public String getRejectionMessage() {
        return rejectionMessage;
    }

    public void setRejectionMessage(String rejectionMessage) {
        this.rejectionMessage = rejectionMessage;
    }

    public SubscriptionOrigin getImportOrigin() {
        return importOrigin;
    }

    public void setImportOrigin(SubscriptionOrigin importOrigin) {
        this.importOrigin = importOrigin;
    }
}
