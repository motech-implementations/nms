package org.motechproject.nms.region.domain;

import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;
import org.motechproject.mds.annotations.InstanceLifecycleListeners;
import org.motechproject.mds.domain.MdsEntity;
import org.motechproject.nms.tracking.annotation.TrackClass;
import org.motechproject.nms.tracking.annotation.TrackFields;

import javax.jdo.annotations.Column;

/**
 * Created by beehyvsc on 22/8/18.
 */
@Entity(tableName = "nms_village_healthsubfacility")
@TrackClass
@TrackFields
@InstanceLifecycleListeners
public class VillageHealthSubFacility extends MdsEntity{

    @Field(name = "village_id")
    @Column
    private Long villageIdOID;

    @Field(name = "healthSubFacility_id")
    @Column
    private Long healthSubFacilityIdOID;

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
}
