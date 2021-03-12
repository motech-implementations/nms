package org.motechproject.nms.region.domain;

import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;
import org.motechproject.mds.annotations.InstanceLifecycleListeners;
import org.motechproject.mds.domain.MdsEntity;
import org.motechproject.nms.tracking.annotation.TrackClass;
import org.motechproject.nms.tracking.annotation.TrackFields;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.Unique;

/**
 * Created by beehyvsc on 22/8/18.
 */
@Entity(tableName = "nms_village_healthsubfacility")
@TrackClass
@TrackFields
@InstanceLifecycleListeners
@Unique(name = "UNIQUE_VILLAGE_HEALTHSUBFACILITY", members = { "villageIdOID", "healthSubFacilityIdOID" })
public class VillageHealthSubFacility extends MdsEntity{

    @Field(name = "village_id")
    @Column
    private Long villageIdOID;

    @Field(name = "healthSubFacility_id")
    @Column
    private Long healthSubFacilityIdOID;

    @Field
    private Long districtCode;

    @Field
    private String talukaCode;

    @Field
    private Long healthFacilityCode;

    @Field
    private String villageName;

    public Long getVillageIdOID() {
        return villageIdOID;
    }

    public void setVillageIdOID(Long villageIdOID) {
        this.villageIdOID = villageIdOID;
    }

    public Long getHealthSubFacilityIdOID() {
        return healthSubFacilityIdOID;
    }

    public void setHealthSubFacilityIdOID(Long healthSubFacilityIdOID) {
        this.healthSubFacilityIdOID = healthSubFacilityIdOID;
    }

    public Long getDistrictCode() {
        return districtCode;
    }

    public void setDistrictCode(Long districtCode) {
        this.districtCode = districtCode;
    }

    public String getTalukaCode() {
        return talukaCode;
    }

    public void setTalukaCode(String talukaCode) {
        this.talukaCode = talukaCode;
    }

    public Long getHealthFacilityCode() {
        return healthFacilityCode;
    }

    public void setHealthFacilityCode(Long healthFacilityCode) {
        this.healthFacilityCode = healthFacilityCode;
    }

    public String getVillageName() {
        return villageName;
    }

    public void setVillageName(String villageName) {
        this.villageName = villageName;
    }
}
