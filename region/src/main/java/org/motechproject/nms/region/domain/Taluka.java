package org.motechproject.nms.region.domain;

import org.motechproject.mds.annotations.Cascade;
import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;
import org.motechproject.mds.domain.MdsEntity;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

@Entity(tableName = "nms_talukas")
@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable = "true")
public class Taluka extends MdsEntity {

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
    @Column(allowsNull = "false", length = 7)
    @NotNull
    @Size(min = 1, max = 7)
    // File from MoH shows a 50 char string in taluka file, but a 7 char string in village.
    // Sample data shows string (i.e. '0005')
    // Email thread says number.   grrrr
    private String code;

    @Field
    @Column(allowsNull = "false")
    @NotNull
    private Integer identity;

    @Field
    @Column(allowsNull = "false")
    @NotNull
    private District district;

    @Field
    @Cascade(delete = true)
    @Persistent(mappedBy = "taluka", defaultFetchGroup = "true")
    private List<HealthBlock> healthBlocks;

    @Field
    @Cascade(delete = true)
    @Persistent(mappedBy = "taluka", defaultFetchGroup = "true")
    private List<Village> villages;

    public Taluka() {
        this.healthBlocks = new ArrayList<>();
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

    public List<HealthBlock> getHealthBlocks() {
        return healthBlocks;
    }

    public void setHealthBlocks(List<HealthBlock> healthBlocks) {
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
                ", district=" + district +
                '}';
    }
}
