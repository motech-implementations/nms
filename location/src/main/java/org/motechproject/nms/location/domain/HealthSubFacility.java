package org.motechproject.nms.location.domain;

import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;
import org.motechproject.mds.domain.MdsEntity;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.Unique;
import javax.validation.constraints.NotNull;

@Entity(tableName = "nms_health_sub_facilities", recordHistory = true)
public class HealthSubFacility extends MdsEntity {

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
    @Column(allowsNull = "false")
    @NotNull
    private HealthFacility healthFacility;

    public HealthSubFacility() {
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

    public HealthFacility getHealthFacility() {
        return healthFacility;
    }

    public void setHealthFacility(HealthFacility healthFacility) {
        this.healthFacility = healthFacility;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        HealthSubFacility that = (HealthSubFacility) o;

        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }
        return !(code != null ? !code.equals(that.code) : that.code != null);

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (code != null ? code.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "HealthSubFacility{" +
                "name='" + name + '\'' +
                ", code=" + code +
                ", healthFacility=" + healthFacility +
                '}';
    }
}
