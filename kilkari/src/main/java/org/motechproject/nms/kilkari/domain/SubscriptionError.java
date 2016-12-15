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
    private String beneficiaryId; //mcts

    @Field
    private String rchId;

    @Field
    private SubscriptionRejectionReason rejectionReason;

    @Field
    private SubscriptionPackType packType;

    @Field
    private String rejectionMessage;

    @Field
    private BeneficiaryImportOrigin importOrigin;

    public SubscriptionError(long contactNumber, SubscriptionRejectionReason rejectionReason,
                             SubscriptionPackType packType, BeneficiaryImportOrigin importOrigin) {
        this(contactNumber, rejectionReason, packType, null, importOrigin);
    }

    public SubscriptionError(long contactNumber, SubscriptionRejectionReason rejectionReason,
                             SubscriptionPackType packType, String rejectionMessage, BeneficiaryImportOrigin importOrigin) {
        this.contactNumber = contactNumber;
        this.rejectionReason = rejectionReason;
        this.packType = packType;
        this.rejectionMessage = rejectionMessage;
        this.importOrigin = importOrigin;
    }

    public SubscriptionError(long contactNumber, String beneficiaryId, SubscriptionRejectionReason rejectionReason,
                             SubscriptionPackType packType, String rejectionMessage, BeneficiaryImportOrigin importOrigin) {
        this.contactNumber = contactNumber;
        this.beneficiaryId = beneficiaryId;
        this.rejectionReason = rejectionReason;
        this.packType = packType;
        this.rejectionMessage = rejectionMessage;
        this.importOrigin = importOrigin;
    }

    public SubscriptionError(long contactNumber, String beneficiaryId, String rchId, SubscriptionRejectionReason rejectionReason,
                             SubscriptionPackType packType, String rejectionMessage, BeneficiaryImportOrigin importOrigin) {
        this.contactNumber = contactNumber;
        this.beneficiaryId = beneficiaryId;
        this.rchId = rchId;
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

    public String getRchId() {
        return rchId;
    }

    public void setRchId(String rchId) {
        this.rchId = rchId;
    }

    public BeneficiaryImportOrigin getImportOrigin() {
        return importOrigin;
    }

    public void setImportOrigin(BeneficiaryImportOrigin importOrigin) {
        this.importOrigin = importOrigin;
    }
}
