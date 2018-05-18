package org.motechproject.nms.kilkari.domain;

import org.joda.time.DateTime;
import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;

import javax.jdo.annotations.Column;
import javax.validation.constraints.NotNull;


@Entity(tableName = "nms_reactivated_beneficiary")
public class ReactivatedBeneficiaryAudit {


    @Field
    @NotNull
    private String externalId;

    @Field
    @Column(allowsNull = "false")
    private SubscriptionOrigin origin;

    @Field
    private DeactivationReason deactivationReason;

    @Field
    private DateTime serviceReactivationDate;

    @Field
    private SubscriptionPack subscriptionPack;

    @Field
    private DateTime deactivationDate;

    public ReactivatedBeneficiaryAudit() {

    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public SubscriptionOrigin getOrigin() {
        return origin;
    }

    public void setOrigin(SubscriptionOrigin origin) {
        this.origin = origin;
    }

    public DeactivationReason getDeactivationReason() {
        return deactivationReason;
    }

    public void setDeactivationReason(DeactivationReason deactivationReason) {
        this.deactivationReason = deactivationReason;
    }

    public DateTime getServiceReactivationDate() {
        return serviceReactivationDate;
    }

    public void setServiceReactivationDate(DateTime serviceReactivationDate) {
        this.serviceReactivationDate = serviceReactivationDate;
    }

    public DateTime getDeactivationDate() {
        return deactivationDate;
    }

    public void setDeactivationDate(DateTime deactivationDate) {
        this.deactivationDate = deactivationDate;
    }

    public SubscriptionPack getSubscriptionPack() {
        return subscriptionPack;
    }

    public void setSubscriptionPack(SubscriptionPack subscriptionPack) {
        this.subscriptionPack = subscriptionPack;
    }
}
