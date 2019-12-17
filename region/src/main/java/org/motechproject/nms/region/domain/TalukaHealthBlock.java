package org.motechproject.nms.region.domain;

/**
 * Created by beehyvsc on 22/8/18.
 */

import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;
import org.motechproject.mds.annotations.InstanceLifecycleListeners;
import org.motechproject.mds.domain.MdsEntity;
import org.motechproject.nms.tracking.annotation.TrackClass;
import org.motechproject.nms.tracking.annotation.TrackFields;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.Unique;

@Entity(tableName = "nms_taluka_healthblock")
@TrackClass
@TrackFields
@InstanceLifecycleListeners
@Unique(name = "UNIQUE_TALUKA_HEALTHBLOCK", members = { "talukaIdOID", "healthBlockIdOID" })
public class TalukaHealthBlock extends MdsEntity{

    @Field(name = "taluka_id")
    @Column
    private Long talukaIdOID;

    @Field(name = "healthBlock_id")
    @Column
    private Long healthBlockIdOID;

    public Long getTalukaIdOID() {
        return talukaIdOID;
    }

    public void setTalukaIdOID(Long talukaIdOID) {
        this.talukaIdOID = talukaIdOID;
    }

    public Long getHealthBlockIdOID() {
        return healthBlockIdOID;
    }

    public void setHealthBlockIdOID(Long healthBlockIdOID) {
        this.healthBlockIdOID = healthBlockIdOID;
    }
}
