package org.motechproject.nms.region.domain;

import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;
import org.motechproject.mds.domain.MdsEntity;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.Unique;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity(tableName = "nms_health_facility_types")
public class HealthFacilityType extends MdsEntity {
    @Field
    @Column(allowsNull = "false", length = 100)
    @NotNull
    @Size(min = 1, max = 100)
    private String name;

    @Field
    @Unique
    @Column(allowsNull = "false")
    @NotNull
    private Long code;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        HealthFacilityType hft = (HealthFacilityType) o;

        if (name != null ? !name.equals(hft.name) : hft.name != null) {
            return false;
        }
        return !(code != null ? !code.equals(hft.code) : hft.code != null);

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (code != null ? code.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "HealthFacilityType{" +
                "name='" + name + '\'' +
                ", code=" + code +
                '}';
    }
}
