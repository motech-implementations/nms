package org.motechproject.nms.location.domain;

import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;
import org.motechproject.mds.annotations.UIDisplayable;

/**
 * This class Models data for District location records
 */
@Entity(recordHistory = true)
public class District {

    @Field
    @UIDisplayable(position = 0)
    private String name;

    @Field
    @UIDisplayable(position = 1)
    private Long districtCode;

    @Field
    @UIDisplayable(position = 2)
    private State state;


    public District() {
        super();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getDistrictCode() {
        return districtCode;
    }

    public void setDistrictCode(Long districtCode) {
        this.districtCode = districtCode;
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
        if (!(o instanceof District)) {
            return false;
        }

        District district = (District) o;

        if (!this.getDistrictCode().equals(district.getDistrictCode())) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (districtCode != null ? districtCode.hashCode() : 0);
        result = 31 * result + (state != null ? state.hashCode() : 0);
        return result;
    }

    /**
     * This method override the toString method to create string for District code, Taluka and
     * State Code for the instance variables
     *
     * @return The string of the District code, Taluka and State Code of the instance variables.
     */
    @Override
    public String toString() {
        return "District{" +
                "name='" + name +
                ", districtCode=" + districtCode +
                '}';
    }
}
