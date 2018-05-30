package org.motechproject.nms.region.domain;

import org.codehaus.jackson.annotate.JsonBackReference;
import org.codehaus.jackson.annotate.JsonManagedReference;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity(tableName = "nms_talukas")
@Unique(name = "UNIQUE_DISTRICT_CODE", members = { "district", "code" })
@TrackClass
@TrackFields
@InstanceLifecycleListeners
public class Taluka extends MdsEntity {

    @Field
    @Column(allowsNull = "false", length = 150)
    @NotNull
    @Size(min = 1, max = 150)
    private String name;

    @Field
    @Column(length = 150)
    @Size(min = 1, max = 150)
    private String regionalName;

    @Field
    @Column(allowsNull = "false", length = 7)
    @NotNull
    @Size(min = 1, max = 7)
    // File from MoH shows a 50 char string in taluka file, but a 7 char string in village.
    // Sample data shows string (i.e. '0005')
    // Email thread says number.   grrrr
    private String code;

    @Field
    private Integer identity;

    @Field
    @Column(allowsNull = "false")
    @NotNull
    @JsonBackReference
    private District district;

    @Persistent(mappedBy = "talukas", defaultFetchGroup = "false")
    @JsonManagedReference
    private Set<HealthBlock> healthBlocks;

    @Field
    @Cascade(delete = true)
    @Persistent(mappedBy = "taluka", defaultFetchGroup = "false")
    @JsonManagedReference
    private List<Village> villages;

    public Taluka() {
        this.healthBlocks = new HashSet<>();
        this.villages = new ArrayList<>();
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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Integer getIdentity() {
        return identity;
    }

    public void setIdentity(Integer identity) {
        this.identity = identity;
    }

    public District getDistrict() {
        return district;
    }

    public void setDistrict(District district) {
        this.district = district;
    }

    public Set<HealthBlock> getHealthBlocks() {
        return healthBlocks;
    }

    public void setHealthBlocks(Set<HealthBlock> healthBlocks) {
        this.healthBlocks = healthBlocks;
    }

    public List<Village> getVillages() {
        return villages;
    }

    public void setVillages(List<Village> villages) {
        this.villages = villages;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Taluka taluka = (Taluka) o;

        if (name != null ? !name.equals(taluka.name) : taluka.name != null) {
            return false;
        }
        return !(code != null ? !code.equals(taluka.code) : taluka.code != null);

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (code != null ? code.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Taluka{" +
                "name='" + name + '\'' +
                ", code=" + code +
                '}';
    }
}
