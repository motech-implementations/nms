package org.motechproject.nms.region.domain;

import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;
import org.motechproject.mds.domain.MdsEntity;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.Unique;
import javax.validation.constraints.NotNull;

/**
 * Created by beehyv on 28/5/18.
 */
@Entity(tableName = "nms_taluka_healthblock")
@Unique(name = "UNIQUE_TALUKA_HEALTHBLOCK", members = { "taluka", "healthBlock" })
public class TalukaHealthBlockMap extends MdsEntity {

    @Field
    @Column(allowsNull = "false")
    @NotNull
    Taluka taluka;

    @Field
    @Column(allowsNull = "false")
    @NotNull
    HealthBlock healthBlock;

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
}
