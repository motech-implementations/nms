package org.motechproject.nms.flw.domain;

import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;
import org.motechproject.mds.annotations.InstanceLifecycleListeners;
import org.motechproject.mds.domain.MdsEntity;
import org.motechproject.nms.region.domain.State;
import org.motechproject.nms.tracking.annotation.TrackClass;
import org.motechproject.nms.tracking.annotation.TrackFields;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.Unique;
import javax.validation.constraints.NotNull;

/**
 * A state that has the whitelist enabled will appear in this table
 */
@Entity(tableName = "nms_whitelisted_states")
@TrackClass
@TrackFields
@InstanceLifecycleListeners
public class WhitelistState extends MdsEntity {
    @Field
    @NotNull
    @Unique
    @Column(allowsNull = "false")
    private State state;

    public WhitelistState(State state) {
        this.state = state;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        WhitelistState whitelistState = (WhitelistState) o;

        return !(state != null ? !state.equals(whitelistState.state) : whitelistState.state != null);

    }

    @Override
    public int hashCode() {
        return state != null ? state.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "WhitelistState{" +
                "state=" + state +
                '}';
    }
}
