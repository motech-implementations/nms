package org.motechproject.nms.location.domain;

import org.motechproject.mds.annotations.Cascade;
import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;
import org.motechproject.mds.annotations.UIDisplayable;
import org.motechproject.mds.domain.MdsEntity;

import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.Unique;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Entity(tableName = "nms_health_facilities")
public class HealthFacility extends MdsEntity {

    @Field
    @UIDisplayable(position = 0)
    @NotNull
    private String name;

    @Field
    @Unique
    @UIDisplayable(position = 1)
    @NotNull
    private Long code;

    @Field
    @UIDisplayable(position = 2)
    @NotNull
    private Integer healthFacilityType;

    @Field
    @UIDisplayable(position = 3)
    @NotNull
    private HealthBlock healthBlock;

    @Field
    @Cascade(delete = true)
    @Persistent(mappedBy = "healthFacility", defaultFetchGroup = "true")
    private List<HealthSubFacility> healthSubFacilities;

    public HealthFacility() {
        this.healthSubFacilities = new ArrayList<>();
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

    public Integer getHealthFacilityType() {
        return healthFacilityType;
    }

    public void setHealthFacilityType(Integer healthFacilityType) {
        this.healthFacilityType = healthFacilityType;
    }

    public HealthBlock getHealthBlock() {
        return healthBlock;
    }

    public void setHealthBlock(HealthBlock healthBlock) {
        this.healthBlock = healthBlock;
    }

    public List<HealthSubFacility> getHealthSubFacilities() {
        return healthSubFacilities;
    }

    public void setHealthSubFacilities(List<HealthSubFacility> healthSubFacilities) {
        this.healthSubFacilities = healthSubFacilities;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        HealthFacility that = (HealthFacility) o;

        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }
        if (code != null ? !code.equals(that.code) : that.code != null) {
            return false;
        }
        return !(healthFacilityType != null ? !healthFacilityType
                .equals(that.healthFacilityType) : that.healthFacilityType != null);

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (code != null ? code.hashCode() : 0);
        result = 31 * result + (healthFacilityType != null ? healthFacilityType.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "HealthFacility{" +
                "name='" + name + '\'' +
                ", code=" + code +
                ", healthFacilityType=" + healthFacilityType +
                ", healthBlock=" + healthBlock +
                '}';
    }
}
