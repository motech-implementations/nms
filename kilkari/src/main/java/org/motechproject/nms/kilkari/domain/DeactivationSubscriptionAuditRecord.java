package org.motechproject.nms.kilkari.domain;

import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;

import javax.jdo.annotations.Column;
import javax.validation.constraints.NotNull;

@Entity(tableName = "nms_deactivation_subscription_audit_records")
public class DeactivationSubscriptionAuditRecord {

    public static final int MAX_OUTCOME_LENGTH = 1000; // Includes the "Failure: " string

    @Field
    @NotNull
    private String subscriptionId;

    @Field
    @NotNull
    private Long subscriberId;

    @Field
    @NotNull
    private SubscriptionOrigin subscriptionOrigin;

    @Field
    @NotNull
    private Long msisdn;

    @Field
    @NotNull
    private SubscriptionStatus preStatus;

    @Field
    @NotNull
    private AuditStatus auditStatus;

    @Field
    @Column(length = MAX_OUTCOME_LENGTH)
    private String outcome;

    public SubscriptionStatus getPreStatus() {
        return preStatus;
    }

    public void setPreStatus(SubscriptionStatus preStatus) {
        this.preStatus = preStatus;
    }

    public AuditStatus getAuditStatus() {
        return auditStatus;
    }

    public void setAuditStatus(AuditStatus auditStatus) {
        this.auditStatus = auditStatus;
    }

    public String getOutcome() {
        return outcome;
    }

    public void setOutcome(String outcome) {
        this.outcome = outcome;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public Long getSubscriberId() {
        return subscriberId;
    }

    public void setSubscriberId(Long subscriberId) {
        this.subscriberId = subscriberId;
    }

    public SubscriptionOrigin getSubscriptionOrigin() {
        return subscriptionOrigin;
    }

    public void setSubscriptionOrigin(SubscriptionOrigin subscriptionOrigin) {
        this.subscriptionOrigin = subscriptionOrigin;
    }

    public Long getMsisdn() {
        return msisdn;
    }

    public void setMsisdn(Long msisdn) {
        this.msisdn = msisdn;
    }

    public DeactivationSubscriptionAuditRecord(String subscriptionId, Long subscriberId, SubscriptionOrigin subscriptionOrigin, Long msisdn, SubscriptionStatus preStatus, AuditStatus auditStatus, String outcome) {
        this.subscriptionId = subscriptionId;
        this.subscriberId = subscriberId;
        this.subscriptionOrigin = subscriptionOrigin;
        this.msisdn = msisdn;
        this.preStatus = preStatus;
        this.auditStatus = auditStatus;
        this.outcome = outcome;
    }
}
