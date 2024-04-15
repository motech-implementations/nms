package org.motechproject.nms.kilkari.domain;

import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;

@Entity(tableName = "nms_whatsapp_welcome_message")
public class WhatsappWCMsg {
    @Field
    private Subscription subscription;

    public WhatsappWCMsg() {
    }

    public WhatsappWCMsg(Subscription subscription) {
        this.subscription = subscription;
    }

    public Subscription getSubscription() {
        return subscription;
    }

    public void setSubscription(Subscription subscription) {
        this.subscription = subscription;
    }

}
