package org.motechproject.nms.kilkari.domain;

import org.joda.time.DateTime;
import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;

import javax.jdo.annotations.Column;
import javax.validation.constraints.NotNull;

/**
 * Record for users who are deactivated or completed their subscriptions.
 * <p>
 * Created by ajai on 5/6/17.
 */
@Entity(tableName = "nms_deactivated_beneficiary")
public class DeactivatedBeneficiary {


    @Field
    @NotNull
    private String externalId;

    @Field
    @Column(allowsNull = "false")
    @NotNull
    private SubscriptionOrigin origin;

    @Field
    private DeactivationReason deactivationReason;

    @Field
    private boolean completedSubscription;

    @Field
    private DateTime serviceStartDate;

    @Field
    private DateTime deactivationDate;

    public DeactivatedBeneficiary() {

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

    public boolean isCompletedSubscription() {
        return completedSubscription;
    }

    public void setCompletedSubscription(boolean completedSubscription) {
        this.completedSubscription = completedSubscription;
    }

    public DateTime getServiceStartDate() {
        return serviceStartDate;
    }

    public void setServiceStartDate(DateTime serviceStartDate) {
        this.serviceStartDate = serviceStartDate;
    }

    public DateTime getDeactivationDate() {
        return deactivationDate;
    }

    public void setDeactivationDate(DateTime deactivationDate) {
        this.deactivationDate = deactivationDate;
    }

}
