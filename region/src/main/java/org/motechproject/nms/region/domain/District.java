package org.motechproject.nms.region.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import org.motechproject.mds.annotations.Cascade;
import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;
import org.motechproject.mds.annotations.InstanceLifecycleListeners;
import org.motechproject.mds.domain.MdsEntity;
import org.motechproject.nms.tracking.annotation.TrackClass;
import org.motechproject.nms.tracking.annotation.TrackFields;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.Unique;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

/**
 * This class Models data for District location records
 */
@Entity(tableName = "nms_districts")
@Unique(name = "UNIQUE_STATE_CODE", members = { "state", "code" })
@TrackClass
@TrackFields
@InstanceLifecycleListeners
public class District extends MdsEntity {

    @Field
    @Column(allowsNull = "false", length = 100)
    @NotNull
    @Size(min = 1, max = 100)
    private String name;

    @Field
    @Column(allowsNull = "false", length = 100)
    @NotNull
    @Size(min = 1, max = 100)
    private String regionalName;

    @Field
    @Column(allowsNull = "false")
    @NotNull
    private Long code;

    @Field
    @Persistent(defaultFetchGroup = "false")
    @Column(allowsNull = "false")
    @NotNull
    @JsonBackReference
    private State state;

    @Field
    @Cascade(delete = true)
    @Persistent(mappedBy = "district", defaultFetchGroup = "false")
    @JsonManagedReference
    private ArrayList<Taluka> talukas;

    @Field
    @Cascade(delete = true)
    @Persistent(mappedBy = "district", defaultFetchGroup = "false")
    @JsonManagedReference
    private ArrayList<HealthBlock> healthBlocks;

    @Field
    @JsonBackReference
    private Circle circle;

    @Field
    @Persistent(defaultFetchGroup = "true")
    private Language language;

    public District() {
        this.talukas = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRegionalName() {
        return regionalName;
    }

    public void setRegionalName(String regionalName) {
        this.regionalName = regionalName;
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

    public ArrayList<Taluka> getTalukas() {
        return talukas;
    }

    public void setTalukas(ArrayList<Taluka> talukas) {
        this.talukas = talukas;
    }

    public ArrayList<HealthBlock> getHealthBlocks() {
        return healthBlocks;
    }

    public void setHealthBlocks(ArrayList<HealthBlock> healthBlocks) {
        this.healthBlocks = healthBlocks;
    }

    public Circle getCircle() {
        return circle;
    }

    public void setCircle(Circle circle) {
        this.circle = circle;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
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
                '}';
    }
}
