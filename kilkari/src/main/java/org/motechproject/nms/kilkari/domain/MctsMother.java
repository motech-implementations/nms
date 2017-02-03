package org.motechproject.nms.kilkari.domain;

import org.joda.time.DateTime;
import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;
import org.motechproject.mds.annotations.InstanceLifecycleListeners;
import org.motechproject.nms.tracking.annotation.TrackClass;
import org.motechproject.nms.tracking.annotation.TrackFields;

@Entity(tableName = "nms_mcts_mothers")
@TrackClass
@TrackFields
@InstanceLifecycleListeners
public class MctsMother extends MctsBeneficiary {

    @Field
    private DateTime dateOfBirth; // this field is needed to calculate mother's age for some Kilkari reports

    @Field
    private Long caseNo;

    public MctsMother(String beneficiaryId) {
        super(beneficiaryId);
    }

    public MctsMother(String rchId, String beneficiaryId) {
        super(rchId, beneficiaryId);
    }

    public Long getCaseNo() {
        return caseNo;
    }

    public void setCaseNo(Long caseNo) {
        this.caseNo = caseNo;
    }

    public DateTime getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(DateTime dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public void deepCopyFrom(MctsMother other) {
        setName(other.getName());
        setDateOfBirth(other.getDateOfBirth());
        setState(other.getState());
        setDistrict(other.getDistrict());
        setTaluka(other.getTaluka());
        setHealthBlock(other.getHealthBlock());
        setHealthFacility(other.getHealthFacility());
        setHealthSubFacility(other.getHealthSubFacility());
        setVillage(other.getVillage());
        setUpdatedDateNic(other.getUpdatedDateNic());
    }
}
