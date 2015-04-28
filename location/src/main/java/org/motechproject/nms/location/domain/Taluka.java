package org.motechproject.nms.location.domain;

import org.motechproject.mds.annotations.Cascade;
import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;
import org.motechproject.mds.annotations.UIDisplayable;
import org.motechproject.mds.domain.MdsEntity;

import javax.jdo.annotations.Persistent;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Entity(tableName = "nms_talukas")
public class Taluka extends MdsEntity {

    @Field
    @UIDisplayable(position = 0)
    @NotNull
    private String name;

    @Field
    @UIDisplayable(position = 1)
    @NotNull
    private Long code;

    @Field
    @UIDisplayable(position = 2)
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

    public Long getCode() {
        return code;
    }

    public void setCode(Long code) {
        this.code = code;
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
