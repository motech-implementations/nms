package org.motechproject.nms.kilkari.domain;

import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;

/**
 * Models the kinds of message campaign (i.e. pack) a subscriber can subscribe to, for example antenatal or
 * postnatal
 */
@Entity(tableName = "nms_subscription_packs")
public class SubscriptionPack {

    @Field
    private String name;

    @Field
    private SubscriptionPackType type;

    public SubscriptionPack() {
    }

    public SubscriptionPack(String name, SubscriptionPackType type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SubscriptionPackType getType() {
        return type;
    }

    public void setType(SubscriptionPackType type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SubscriptionPack that = (SubscriptionPack) o;

        return name.equals(that.name);

    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return "SubscriptionPack{" +
                "name='" + name + '\'' +
                '}';
    }
}
