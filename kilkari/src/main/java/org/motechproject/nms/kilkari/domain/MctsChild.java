package org.motechproject.nms.kilkari.domain;


import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;
import org.motechproject.mds.annotations.InstanceLifecycleListeners;
import org.motechproject.nms.tracking.annotation.TrackClass;
import org.motechproject.nms.tracking.annotation.TrackFields;


@Entity(tableName = "nms_mcts_children")
@TrackClass
@TrackFields
@InstanceLifecycleListeners
public class MctsChild extends MctsBeneficiary {

    @Field
    private MctsMother mother;

    public MctsChild(String beneficiaryId) {
        super(beneficiaryId);
    }

    public MctsChild(String beneficiaryId, String name, MctsMother mother) {
        super(beneficiaryId, name);
        this.mother = mother;
    }

    public MctsMother getMother() {
        return mother;
    }

    public void setMother(MctsMother mother) {
        this.mother = mother;
    }

    public void deepCopyFrom(MctsChild other) {
        setName(other.getName());
        setMother(other.getMother());
        setState(other.getState());
        setDistrict(other.getDistrict());
        setTaluka(other.getTaluka());
        setHealthBlock(other.getHealthBlock());
        setHealthFacility(other.getHealthFacility());
        setHealthSubFacility(other.getHealthSubFacility());
        setVillage(other.getVillage());
    }
}
