package org.motechproject.nms.kilkari.domain;

import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;
import org.motechproject.nms.region.domain.District;
import org.motechproject.nms.region.domain.HealthBlock;
import org.motechproject.nms.region.domain.HealthFacility;
import org.motechproject.nms.region.domain.State;
import org.motechproject.nms.region.domain.Taluka;
import org.motechproject.nms.region.domain.Village;

import javax.jdo.annotations.Unique;
import javax.validation.constraints.NotNull;

/**
 * A beneficiary (mother or child) sourced from MCTS.
 */
@Entity
public abstract class MctsBeneficiary {

    // 18-digit IDs are used for most states but not all, so a strict length constraint cannot be set for this column
    @Field
    @Unique
    @NotNull
    private String beneficiaryId;

    @Field
    private String name;

    @Field
    private State state;

    @Field
    private District district;

    @Field
    private Taluka taluka;

    @Field
    private HealthBlock healthBlock;

    @Field
    private HealthFacility primaryHealthCenter;

    @Field
    private Village village;

    public MctsBeneficiary() {
    }

    public MctsBeneficiary(String beneficiaryId) {
        this(beneficiaryId, null);
    }

    public MctsBeneficiary(String beneficiaryId, String name) {
        this.beneficiaryId = beneficiaryId;
        this.name = name;
    }

    public String getBeneficiaryId() {
        return beneficiaryId;
    }

    public void setBeneficiaryId(String beneficiaryId) {
        this.beneficiaryId = beneficiaryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public District getDistrict() {
        return district;
    }

    public void setDistrict(District district) {
        this.district = district;
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

    public HealthFacility getPrimaryHealthCenter() {
        return primaryHealthCenter;
    }

    public void setPrimaryHealthCenter(HealthFacility primaryHealthCenter) {
        this.primaryHealthCenter = primaryHealthCenter;
    }

    public Village getVillage() {
        return village;
    }

    public void setVillage(Village village) {
        this.village = village;
    }
}
