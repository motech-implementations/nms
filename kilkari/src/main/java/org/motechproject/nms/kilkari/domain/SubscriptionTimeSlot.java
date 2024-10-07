package org.motechproject.nms.kilkari.domain;

import org.joda.time.DateTime;
import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;

import javax.persistence.Id;

@Entity(tableName = "nms_subscriptions_time_slot")
public class SubscriptionTimeSlot {

    @Id
    @Field
    private Long subscription_id;

    @Field
    private String subscriptionId;

    @Field
    private Integer timeStamp1;

    @Field
    private Integer timeStamp2;

    @Field
    private Integer timeStamp3;

    @Field
    private DateTime creationDate;

    @Field
    private DateTime modificationDate;

    public SubscriptionTimeSlot() {

    }

    public DateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(DateTime creationDate) {
        this.creationDate = creationDate;
    }

    public DateTime getModificationDate() {
        return modificationDate;
    }

    public void setModificationDate(DateTime modificationDate) {
        this.modificationDate = modificationDate;
    }

    public Long getSubscription_id() {return subscription_id;}

    public void setSubscription_id(Long subscription_id) {this.subscription_id = subscription_id;}

    public void setSubscriptionId(String subscriptionId) {this.subscriptionId = subscriptionId;}

    public Integer getTimeStamp1() {
        return timeStamp1;
    }

    public void setTimeStamp1(Integer timeStamp1) {
        this.timeStamp1 = timeStamp1;
    }

    public Integer getTimeStamp2() {
        return timeStamp2;
    }

    public void setTimeStamp2(Integer timeStamp2) {
        this.timeStamp2 = timeStamp2;
    }

    public Integer getTimeStamp3() {
        return timeStamp3;
    }


    public void setTimeStamp3(Integer timeStamp3) {
        this.timeStamp3 = timeStamp3;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public SubscriptionTimeSlot(Integer timeStamp1, Integer timeStamp2, Integer timeStamp3, String subscriptionId, DateTime creationDate, DateTime modificationDate) {
        this.timeStamp1 = timeStamp1;
        this.timeStamp2 = timeStamp2;
        this.timeStamp3 = timeStamp3;
        this.subscriptionId = subscriptionId;
        this.creationDate = creationDate;
        this.modificationDate = modificationDate;
    }

}
