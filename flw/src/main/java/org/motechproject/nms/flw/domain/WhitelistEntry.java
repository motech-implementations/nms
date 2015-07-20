package org.motechproject.nms.flw.domain;

import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;
import org.motechproject.mds.annotations.InstanceLifecycleListeners;
import org.motechproject.nms.region.domain.State;
import org.motechproject.nms.tracking.annotation.TrackClass;
import org.motechproject.nms.tracking.annotation.TrackFields;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.Unique;
import javax.validation.constraints.NotNull;

@Entity(tableName = "nms_whitelist_entries")
@Unique(name = "UNIQUE_STATE_CONTACT_NUMBER_COMPOSITE_IDX", members = { "state", "contactNumber" })
@TrackClass
@TrackFields
@InstanceLifecycleListeners
public class WhitelistEntry {
    public static final int FIELD_SIZE_10 = 10;

    @Field
    @NotNull
    private State state;

    @Field(required = true)
    @Column(length = FIELD_SIZE_10)
    @NotNull
    private Long contactNumber;

    public WhitelistEntry(Long contactNumber, State state) {
        this.contactNumber = contactNumber;
        this.state = state;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public Long getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(Long contactNumber) {
        this.contactNumber = contactNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        WhitelistEntry that = (WhitelistEntry) o;

        if (state != null ? !state.equals(that.state) : that.state != null) { return false; }
        return !(contactNumber != null ? !contactNumber.equals(that.contactNumber) : that.contactNumber != null);

    }

    @Override
    public int hashCode() {
        int result = state != null ? state.hashCode() : 0;
        result = 31 * result + (contactNumber != null ? contactNumber.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "WhitelistEntry{" +
                "state=" + state +
                ", contactNumber='" + contactNumber + '\'' +
                '}';
    }
}
