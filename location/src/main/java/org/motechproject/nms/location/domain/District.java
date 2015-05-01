package org.motechproject.nms.location.domain;

import org.motechproject.mds.annotations.Cascade;
import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;
import org.motechproject.mds.domain.MdsEntity;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.Unique;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * This class Models data for District location records
 */
@Entity(tableName = "nms_districts")
public class District extends MdsEntity {

    @Field
    @Column(allowsNull = "false")
    @NotNull
    private String name;

    @Field
    @Unique
    @Column(allowsNull = "false")
    @NotNull
    private Long code;

    @Field
    @Persistent(defaultFetchGroup = "true")
    @Column(allowsNull = "false")
    @NotNull
    private State state;

    @Field
    @Cascade(delete = true)
    @Persistent(mappedBy = "district", defaultFetchGroup = "true")
    private List<Taluka> talukas;

    public District() {
        this.talukas = new ArrayList();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getCode() {
        return code;
    }

    public void setCode(Long code) {
        this.code = code;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public List getTalukas() {
        return talukas;
    }

    public void setTalukas(List talukas) {
        this.talukas = talukas;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        District district = (District) o;

        if (name != null ? !name.equals(district.name) : district.name != null) {
            return false;
        }
        return !(code != null ? !code.equals(district.code) : district.code != null);

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (code != null ? code.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "District{" +
                "name='" + name + '\'' +
                ", code=" + code +
                ", state=" + state +
                '}';
    }
}
