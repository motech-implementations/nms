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
    private DateTime lastMenstrualPeriod;

    @Field
    private Long maxCaseNo;

    @Field
    private DateTime registrationDate;

    @Field
    private String ashaId;

    public MctsMother(String beneficiaryId) {
        super(beneficiaryId);
    }

    public MctsMother(String rchId, String beneficiaryId) {
        super(rchId, beneficiaryId);
    }

    public DateTime getLastMenstrualPeriod() {
        return lastMenstrualPeriod;
    }

    public void setLastMenstrualPeriod(DateTime lastMenstrualPeriod) {
        this.lastMenstrualPeriod = lastMenstrualPeriod;
    }

    public Long getMaxCaseNo() {
        return maxCaseNo;
    }

    public void setMaxCaseNo(Long maxCaseNo) {
        this.maxCaseNo = maxCaseNo;
    }

    public DateTime getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(DateTime registrationDate) {
        this.registrationDate = registrationDate;
    }

    public String getAshaId(){ return ashaId; }

    public void setAshaId(String ashaId) { this.ashaId = ashaId; }

    public void deepCopyFrom(MctsMother other) {
        setName(other.getName());
        setDateOfBirth(other.getDateOfBirth());
        setLastMenstrualPeriod(other.getLastMenstrualPeriod());
        setState(other.getState());
        setDistrict(other.getDistrict());
        setTaluka(other.getTaluka());
        setHealthBlock(other.getHealthBlock());
        setHealthFacility(other.getHealthFacility());
        setHealthSubFacility(other.getHealthSubFacility());
        setVillage(other.getVillage());
        setUpdatedDateNic(other.getUpdatedDateNic());
        setMaxCaseNo(other.getMaxCaseNo());
        setRegistrationDate(other.getRegistrationDate());
        setAshaId(other.getAshaId());
    }
}
