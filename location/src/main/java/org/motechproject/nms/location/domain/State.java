package org.motechproject.nms.location.domain;

import org.motechproject.mds.annotations.Cascade;
import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;
import org.motechproject.mds.annotations.UIDisplayable;

import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.Unique;
import java.util.Set;

/**
 * This class Models data for State location records
 */
@Entity(recordHistory = true)
public class State {

    @Field
    @UIDisplayable(position = 0)
    private String name;

    @Field
    @Unique
    @UIDisplayable(position = 1)
    private Long stateCode;

    @Field
    @Cascade(delete = true)
    @Persistent(mappedBy = "state", defaultFetchGroup = "true")
    private Set<District> districts;

    public State() {
    }

    public State(String name, Long stateCode) {
        this.name = name;
        this.stateCode = stateCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getStateCode() {
        return stateCode;
    }

    public void setStateCode(Long stateCode) {
        this.stateCode = stateCode;
    }

    public Set<District> getDistricts() {
        return districts;
    }

    public void setDistricts(Set<District> districts) {
        this.districts = districts;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        State state = (State) o;

        if (!name.equals(state.name)) { return false; }
        if (!stateCode.equals(state.stateCode)) { return false; }

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + stateCode.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "State{" +
                "name='" + name + '\'' +
                ", stateCode=" + stateCode +
                ", districts=" + districts +
                '}';
    }
}
