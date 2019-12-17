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

import javax.jdo.annotations.Unique;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.Element;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class Models data for HealthBlock location records
 */
@Entity(tableName = "nms_health_blocks")
@Unique(name = "UNIQUE_STATE_CODE", members = { "stateIdOID", "code" })
@TrackClass
@TrackFields
@InstanceLifecycleListeners
public class HealthBlock extends MdsEntity {

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
    @Column(length = 50)
    @Size(min = 1, max = 50)
    private String hq;

    @Field
    @Column(allowsNull = "false")
    @NotNull
    private Long code;

    @Field
    @Column(allowsNull = "false")
    @NotNull
    @JsonBackReference
    private District district;

    @Field(name = "taluka_id_OID", required = true)
    @Column
    private Long talukaIdOID;


    public Long getStateIdOID() {
        return stateIdOID;
    }

    public void setStateIdOID(Long stateIdOID) {
        this.stateIdOID = stateIdOID;
    }

    @Field(name = "state_id_OID", required = true)
    @Column
    private Long stateIdOID;

//    @Persistent(table="nms_taluka_healthblock", defaultFetchGroup = "false")
//    @Join(column = "healthblock_id")
//    @Element(column = "taluka_id")
//    @JsonManagedReference
//    private Set<Taluka> talukas;


    @Field
    @Cascade(delete = true)
    @Persistent(mappedBy = "healthBlock", defaultFetchGroup = "false")
    @JsonManagedReference
    private List<HealthFacility> healthFacilities;

    public HealthBlock() {
        //this.talukas = new HashSet<>();
        this.healthFacilities = new ArrayList<>();
    }

    public District getDistrict() {
        return district;
    }

    public void setDistrict(District district) {
        this.district = district;
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

    public String getHq() {
        return hq;
    }

    public void setHq(String hq) {
        this.hq = hq;
    }

    public Long getCode() {
        return code;
    }

    public void setCode(Long code) {
        this.code = code;
    }

    public Long getTalukaIdOID() {
        return talukaIdOID;
    }

    public void setTalukaIdOID(Long talukaIdOID) {
        this.talukaIdOID = talukaIdOID;
    }

//    public Set<Taluka> getTalukas() {
//        return talukas;
//    }
//
//    public void setTalukas(Set<Taluka> talukas) {
//        this.talukas = talukas;
//    }

//    public void addTaluka(Taluka taluka) {
//        this.talukas.add(taluka);
//    }
//
//    public void removeTaluka(Taluka taluka) {
//        this.talukas.remove(taluka);
//    }

    public List<HealthFacility> getHealthFacilities() {
        return healthFacilities;
    }

    public void setHealthFacilities(List<HealthFacility> healthFacilities) {
        this.healthFacilities = healthFacilities;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        HealthBlock other = (HealthBlock) o;

        if (name != null ? !name.equals(other.name) : other.name != null) {
            return false;
        }
        return !(code != null ? !code.equals(other.code) : other.code != null);

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (code != null ? code.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "HealthBlock{" +
                "name='" + name + '\'' +
                ", code=" + code +
                '}';
    }
}
