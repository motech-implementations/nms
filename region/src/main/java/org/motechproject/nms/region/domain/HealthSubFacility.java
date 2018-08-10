package org.motechproject.nms.region.domain;

import org.codehaus.jackson.annotate.JsonBackReference;
import org.codehaus.jackson.annotate.JsonManagedReference;
import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;
import org.motechproject.mds.annotations.InstanceLifecycleListeners;
import org.motechproject.mds.domain.MdsEntity;
import org.motechproject.nms.tracking.annotation.TrackClass;
import org.motechproject.nms.tracking.annotation.TrackFields;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.Unique;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.Element;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

@Entity(tableName = "nms_health_sub_facilities")
@Unique(name = "UNIQUE_HEALTH_FACILITY_CODE", members = { "healthFacility", "code" })
@TrackClass
@TrackFields
@InstanceLifecycleListeners
public class HealthSubFacility extends MdsEntity {

    @Field
    @Column(allowsNull = "false", length = 250)
    @NotNull
    @Size(min = 1, max = 250)
    private String name;

    @Field
    @Column(length = 250)
    @Size(min = 1, max = 250)
    private String regionalName;

    @Field
    @Column(allowsNull = "false")
    @NotNull
    private Long code;

    @Field
    @NotNull
    @Column(allowsNull = "false")
    @JsonBackReference
    private Taluka taluka;

    @Field
    @Column(allowsNull = "false")
    @NotNull
    @JsonBackReference
    private HealthFacility healthFacility;

    @Persistent(table="nms_village_healthsubfacility", defaultFetchGroup = "true")
    @Join(column = "healthsubfacility_id")
    @Element(column = "village_id")
    @JsonManagedReference
    private Set<Village> villages;

    public HealthSubFacility() {
        this.villages = new HashSet<>();
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

    public Taluka getTaluka() {
        return taluka;
    }

    public void setTaluka(Taluka taluka) {
        this.taluka = taluka;
    }

    public HealthFacility getHealthFacility() {
        return healthFacility;
    }

    public void setHealthFacility(HealthFacility healthFacility) {
        this.healthFacility = healthFacility;
    }

    public Set<Village> getVillages() {
        return villages;
    }

    public void setVillages(Set<Village> villages) {
        this.villages = villages;
    }

    public void addVillage(Village village) {
        this.villages.add(village);
    }

    public void removeVillage(Village village) {
        this.villages.remove(village);
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
                '}';
    }
}
