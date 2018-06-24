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
import java.util.List;

// TODO: Remove maxFetchDepth once https://applab.atlassian.net/browse/MOTECH-1678 is resolved
@Entity(maxFetchDepth = -1, tableName = "nms_health_facilities")
@Unique(name = "UNIQUE_HEALTH_BLOCK_CODE", members = { "healthBlock", "code" })
@TrackClass
@TrackFields
@InstanceLifecycleListeners
public class HealthFacility extends MdsEntity {

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
    private HealthFacilityType healthFacilityType;

    @Field
    @NotNull
    @Column(allowsNull = "false")
    @JsonBackReference
    private Taluka taluka;

    @Field
    @Column(allowsNull = "false")
    @NotNull
    @JsonBackReference
    private HealthBlock healthBlock;

    @Field
    @Cascade(delete = true)
    @Persistent(mappedBy = "healthFacility", defaultFetchGroup = "false")
    @JsonManagedReference
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

    public HealthFacilityType getHealthFacilityType() {
        return healthFacilityType;
    }

    public void setHealthFacilityType(HealthFacilityType healthFacilityType) {
        this.healthFacilityType = healthFacilityType;
    }

    public Taluka getTaluka() {
        return taluka;
    }

    public void setTaluka(Taluka taluka) {
        this.taluka = taluka;
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

        HealthFacility hf = (HealthFacility) o;

        if (name != null ? !name.equals(hf.name) : hf.name != null) {
            return false;
        }
        return !(code != null ? !code.equals(hf.code) : hf.code != null);

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (code != null ? code.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "HealthFacility{" +
                "name='" + name + '\'' +
                ", code=" + code +
                '}';
    }
}
