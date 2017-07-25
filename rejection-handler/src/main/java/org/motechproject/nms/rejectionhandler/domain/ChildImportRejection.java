package org.motechproject.nms.rejectionhandler.domain;

import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;

import javax.persistence.*;

import java.util.Date;

/**
 * Created by beehyvsc on 12/7/17.
 */
@Entity(tableName = "nms_child_rejects")
public class ChildImportRejection {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Field
    private Long ChildRejectId;

    @Field
    private Long stateId;

    @Field
    private Long districtId;

    @Field
    private String districtName;

    @Field
    private String talukaId;

    @Field
    private String talukaName;

    @Field
    private Long healthBlockId;

    @Field
    private String healthBlockName;

    @Field
    private Long phcId;

    @Field
    private String phcName;

    @Field
    private Long subcentreId;

    @Field
    private String subcentreName;

    @Field
    private Long villageId;

    @Field
    private String villageName;

    @Field
    private Integer yr;

    @Field
    private String cityMaholla;

    @Field
    private String GPVillage;

    @Field
    private String address;

    @Field
    private String idNo;

    @Field
    private String name;

    @Field
    private String mobileNo;

    @Field
    private String motherName;

    @Field
    private String motherId;

    @Field
    private String phoneNumberWhom;

    @Field
    private String whomPhoneNumber;

    @Field
    private String birthDate;

    @Field
    private String placeOfDelivery;

    @Field
    private String bloodGroup;

    @Field
    private String caste;

    @Field
    private String subcenterName1;

    @Field
    private String ANMName;

    @Field
    private String ANMPhone;

    @Field
    private String ashaName;

    @Field
    private String ashaPhone;

    @Field
    private String BCGDt;

    @Field
    private String OPV0Dt;

    @Field
    private String HepatitisB1Dt;

    @Field
    private String DPT1Dt;

    @Field
    private String OPV1Dt;

    @Field
    private String HepatitisB2Dt;

    @Field
    private String DPT2Dt;

    @Field
    private String OPV2Dt;

    @Field
    private String HepatitisB3Dt;

    @Field
    private String DPT3Dt;

    @Field
    private String OPV3Dt;

    @Field
    private String HepatitisB4Dt;

    @Field
    private String MeaslesDt;

    @Field
    private String VitADose1Dt;

    @Field
    private String MRDt;

    @Field
    private String DPTBoosterDt;

    @Field
    private String OPVBoosterDt;

    @Field
    private String VitADose2Dt;

    @Field
    private String VitADose3Dt;

    @Field
    private String JEDt;

    @Field
    private String VitADose9Dt;

    @Field
    private String DT5Dt;

    @Field
    private String TT10Dt;

    @Field
    private String TT16Dt;

    @Field
    private String CLDRegDATE;

    @Field
    private String sex;

    @Field
    private String VitADose5Dt;

    @Field
    private String VitADose6Dt;

    @Field
    private String VitADose7Dt;

    @Field
    private String VitADose8Dt;

    @Field
    private String lastUpdateDate;

    @Field
    private String remarks;

    @Field
    private Integer ANMID;

    @Field
    private Integer ashaID;

    @Field
    private Integer createdBy;

    @Field
    private Integer updatedBy;

    @Field
    private String measles2Dt;

    @Field
    private Double weightOfChild;

    @Field
    private Integer childAadhaarNo;

    @Field
    private Integer childEID;

    @Field
    private String childEIDTime;

    @Field
    private String fatherName;

    @Field
    private String birthCertificateNumber;

    @Field
    private Integer entryType;

    @Field
    private String registrationNo;

    @Field
    private String MCTSMotherIDNo;

    @Field
    private String ExecDate;

    @Field
    private Boolean accepted;

    @Field
    private String rejectionReason;

    @Field
    private String source;

    @Field
    private Date creationDate;

    @Field
    private Date modificationDate;

    @Field
    private String action;

    public Long getChildRejectId() {
        return ChildRejectId;
    }

    public void setChildRejectId(Long childRejectId) {
        ChildRejectId = childRejectId;
    }

    public Long getStateId() {
        return stateId;
    }

    public void setStateId(Long stateId) {
        this.stateId = stateId;
    }

    public Long getDistrictId() {
        return districtId;
    }

    public void setDistrictId(Long districtId) {
        this.districtId = districtId;
    }

    public String getDistrictName() {
        return districtName;
    }

    public void setDistrictName(String districtName) {
        this.districtName = districtName;
    }

    public String getTalukaId() {
        return talukaId;
    }

    public void setTalukaId(String talukaId) {
        this.talukaId = talukaId;
    }

    public String getTalukaName() {
        return talukaName;
    }

    public void setTalukaName(String talukaName) {
        this.talukaName = talukaName;
    }

    public Long getHealthBlockId() {
        return healthBlockId;
    }

    public void setHealthBlockId(Long healthBlockId) {
        this.healthBlockId = healthBlockId;
    }

    public String getHealthBlockName() {
        return healthBlockName;
    }

    public void setHealthBlockName(String healthBlockName) {
        this.healthBlockName = healthBlockName;
    }

    public Long getPhcId() {
        return phcId;
    }

    public void setPhcId(Long phcId) {
        this.phcId = phcId;
    }

    public String getPhcName() {
        return phcName;
    }

    public void setPhcName(String phcName) {
        this.phcName = phcName;
    }

    public Long getSubcentreId() {
        return subcentreId;
    }

    public void setSubcentreId(Long subcentreId) {
        this.subcentreId = subcentreId;
    }

    public String getSubcentreName() {
        return subcentreName;
    }

    public void setSubcentreName(String subcentreName) {
        this.subcentreName = subcentreName;
    }

    public Long getVillageId() {
        return villageId;
    }

    public void setVillageId(Long villageId) {
        this.villageId = villageId;
    }

    public String getVillageName() {
        return villageName;
    }

    public void setVillageName(String villageName) {
        this.villageName = villageName;
    }

    public Integer getYr() {
        return yr;
    }

    public void setYr(Integer yr) {
        this.yr = yr;
    }

    public String getCityMaholla() {
        return cityMaholla;
    }

    public void setCityMaholla(String cityMaholla) {
        this.cityMaholla = cityMaholla;
    }

    public String getGPVillage() {
        return GPVillage;
    }

    public void setGPVillage(String GPVillage) {
        this.GPVillage = GPVillage;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getIdNo() {
        return idNo;
    }

    public void setIdNo(String idNo) {
        this.idNo = idNo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMobileNo() {
        return mobileNo;
    }

    public void setMobileNo(String mobileNo) {
        this.mobileNo = mobileNo;
    }

    public String getMotherName() {
        return motherName;
    }

    public void setMotherName(String motherName) {
        this.motherName = motherName;
    }

    public String getMotherId() {
        return motherId;
    }

    public void setMotherId(String motherId) {
        this.motherId = motherId;
    }

    public String getPhoneNumberWhom() {
        return phoneNumberWhom;
    }

    public void setPhoneNumberWhom(String phoneNumberWhom) {
        this.phoneNumberWhom = phoneNumberWhom;
    }

    public String getWhomPhoneNumber() {
        return whomPhoneNumber;
    }

    public void setWhomPhoneNumber(String whomPhoneNumber) {
        this.whomPhoneNumber = whomPhoneNumber;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public String getPlaceOfDelivery() {
        return placeOfDelivery;
    }

    public void setPlaceOfDelivery(String placeOfDelivery) {
        this.placeOfDelivery = placeOfDelivery;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public String getCaste() {
        return caste;
    }

    public void setCaste(String caste) {
        this.caste = caste;
    }

    public String getSubcenterName1() {
        return subcenterName1;
    }

    public void setSubcenterName1(String subcenterName1) {
        this.subcenterName1 = subcenterName1;
    }

    public String getANMName() {
        return ANMName;
    }

    public void setANMName(String ANMName) {
        this.ANMName = ANMName;
    }

    public String getANMPhone() {
        return ANMPhone;
    }

    public void setANMPhone(String ANMPhone) {
        this.ANMPhone = ANMPhone;
    }

    public String getAshaName() {
        return ashaName;
    }

    public void setAshaName(String ashaName) {
        this.ashaName = ashaName;
    }

    public String getAshaPhone() {
        return ashaPhone;
    }

    public void setAshaPhone(String ashaPhone) {
        this.ashaPhone = ashaPhone;
    }

    public String getBCGDt() {
        return BCGDt;
    }

    public void setBCGDt(String BCGDt) {
        this.BCGDt = BCGDt;
    }

    public String getOPV0Dt() {
        return OPV0Dt;
    }

    public void setOPV0Dt(String OPV0Dt) {
        this.OPV0Dt = OPV0Dt;
    }

    public String getHepatitisB1Dt() {
        return HepatitisB1Dt;
    }

    public void setHepatitisB1Dt(String hepatitisB1Dt) {
        HepatitisB1Dt = hepatitisB1Dt;
    }

    public String getDPT1Dt() {
        return DPT1Dt;
    }

    public void setDPT1Dt(String DPT1Dt) {
        this.DPT1Dt = DPT1Dt;
    }

    public String getOPV1Dt() {
        return OPV1Dt;
    }

    public void setOPV1Dt(String OPV1Dt) {
        this.OPV1Dt = OPV1Dt;
    }

    public String getHepatitisB2Dt() {
        return HepatitisB2Dt;
    }

    public void setHepatitisB2Dt(String hepatitisB2Dt) {
        HepatitisB2Dt = hepatitisB2Dt;
    }

    public String getDPT2Dt() {
        return DPT2Dt;
    }

    public void setDPT2Dt(String DPT2Dt) {
        this.DPT2Dt = DPT2Dt;
    }

    public String getOPV2Dt() {
        return OPV2Dt;
    }

    public void setOPV2Dt(String OPV2Dt) {
        this.OPV2Dt = OPV2Dt;
    }

    public String getHepatitisB3Dt() {
        return HepatitisB3Dt;
    }

    public void setHepatitisB3Dt(String hepatitisB3Dt) {
        HepatitisB3Dt = hepatitisB3Dt;
    }

    public String getDPT3Dt() {
        return DPT3Dt;
    }

    public void setDPT3Dt(String DPT3Dt) {
        this.DPT3Dt = DPT3Dt;
    }

    public String getOPV3Dt() {
        return OPV3Dt;
    }

    public void setOPV3Dt(String OPV3Dt) {
        this.OPV3Dt = OPV3Dt;
    }

    public String getHepatitisB4Dt() {
        return HepatitisB4Dt;
    }

    public void setHepatitisB4Dt(String hepatitisB4Dt) {
        HepatitisB4Dt = hepatitisB4Dt;
    }

    public String getMeaslesDt() {
        return MeaslesDt;
    }

    public void setMeaslesDt(String measlesDt) {
        MeaslesDt = measlesDt;
    }

    public String getVitADose1Dt() {
        return VitADose1Dt;
    }

    public void setVitADose1Dt(String vitADose1Dt) {
        VitADose1Dt = vitADose1Dt;
    }

    public String getMRDt() {
        return MRDt;
    }

    public void setMRDt(String MRDt) {
        this.MRDt = MRDt;
    }

    public String getDPTBoosterDt() {
        return DPTBoosterDt;
    }

    public void setDPTBoosterDt(String DPTBoosterDt) {
        this.DPTBoosterDt = DPTBoosterDt;
    }

    public String getOPVBoosterDt() {
        return OPVBoosterDt;
    }

    public void setOPVBoosterDt(String OPVBoosterDt) {
        this.OPVBoosterDt = OPVBoosterDt;
    }

    public String getVitADose2Dt() {
        return VitADose2Dt;
    }

    public void setVitADose2Dt(String vitADose2Dt) {
        VitADose2Dt = vitADose2Dt;
    }

    public String getVitADose3Dt() {
        return VitADose3Dt;
    }

    public void setVitADose3Dt(String vitADose3Dt) {
        VitADose3Dt = vitADose3Dt;
    }

    public String getJEDt() {
        return JEDt;
    }

    public void setJEDt(String JEDt) {
        this.JEDt = JEDt;
    }

    public String getVitADose9Dt() {
        return VitADose9Dt;
    }

    public void setVitADose9Dt(String vitADose9Dt) {
        VitADose9Dt = vitADose9Dt;
    }

    public String getDT5Dt() {
        return DT5Dt;
    }

    public void setDT5Dt(String DT5Dt) {
        this.DT5Dt = DT5Dt;
    }

    public String getTT10Dt() {
        return TT10Dt;
    }

    public void setTT10Dt(String TT10Dt) {
        this.TT10Dt = TT10Dt;
    }

    public String getTT16Dt() {
        return TT16Dt;
    }

    public void setTT16Dt(String TT16Dt) {
        this.TT16Dt = TT16Dt;
    }

    public String getCLDRegDATE() {
        return CLDRegDATE;
    }

    public void setCLDRegDATE(String CLDRegDATE) {
        this.CLDRegDATE = CLDRegDATE;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getVitADose5Dt() {
        return VitADose5Dt;
    }

    public void setVitADose5Dt(String vitADose5Dt) {
        VitADose5Dt = vitADose5Dt;
    }

    public String getVitADose6Dt() {
        return VitADose6Dt;
    }

    public void setVitADose6Dt(String vitADose6Dt) {
        VitADose6Dt = vitADose6Dt;
    }

    public String getVitADose7Dt() {
        return VitADose7Dt;
    }

    public void setVitADose7Dt(String vitADose7Dt) {
        VitADose7Dt = vitADose7Dt;
    }

    public String getVitADose8Dt() {
        return VitADose8Dt;
    }

    public void setVitADose8Dt(String vitADose8Dt) {
        VitADose8Dt = vitADose8Dt;
    }

    public String getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(String lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public Integer getANMID() {
        return ANMID;
    }

    public void setANMID(Integer ANMID) {
        this.ANMID = ANMID;
    }

    public Integer getAshaID() {
        return ashaID;
    }

    public void setAshaID(Integer ashaID) {
        this.ashaID = ashaID;
    }

    public Integer getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Integer createdBy) {
        this.createdBy = createdBy;
    }

    public Integer getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(Integer updatedBy) {
        this.updatedBy = updatedBy;
    }

    public String getMeasles2Dt() {
        return measles2Dt;
    }

    public void setMeasles2Dt(String measles2Dt) {
        this.measles2Dt = measles2Dt;
    }

    public Double getWeightOfChild() {
        return weightOfChild;
    }

    public void setWeightOfChild(Double weightOfChild) {
        this.weightOfChild = weightOfChild;
    }

    public Integer getChildAadhaarNo() {
        return childAadhaarNo;
    }

    public void setChildAadhaarNo(Integer childAadhaarNo) {
        this.childAadhaarNo = childAadhaarNo;
    }

    public Integer getChildEID() {
        return childEID;
    }

    public void setChildEID(Integer childEID) {
        this.childEID = childEID;
    }

    public String getChildEIDTime() {
        return childEIDTime;
    }

    public void setChildEIDTime(String childEIDTime) {
        this.childEIDTime = childEIDTime;
    }

    public String getFatherName() {
        return fatherName;
    }

    public void setFatherName(String fatherName) {
        this.fatherName = fatherName;
    }

    public String getBirthCertificateNumber() {
        return birthCertificateNumber;
    }

    public void setBirthCertificateNumber(String birthCertificateNumber) {
        this.birthCertificateNumber = birthCertificateNumber;
    }

    public Integer getEntryType() {
        return entryType;
    }

    public void setEntryType(Integer entryType) {
        this.entryType = entryType;
    }

    public String getRegistrationNo() {
        return registrationNo;
    }

    public void setRegistrationNo(String registrationNo) {
        this.registrationNo = registrationNo;
    }

    public String getMCTSMotherIDNo() {
        return MCTSMotherIDNo;
    }

    public void setMCTSMotherIDNo(String MCTSMotherIDNo) {
        this.MCTSMotherIDNo = MCTSMotherIDNo;
    }

    public String getExecDate() {
        return ExecDate;
    }

    public void setExecDate(String execDate) {
        ExecDate = execDate;
    }

    public Boolean getAccepted() {
        return accepted;
    }

    public void setAccepted(Boolean accepted) {
        this.accepted = accepted;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getModificationDate() {
        return modificationDate;
    }

    public void setModificationDate(Date modificationDate) {
        this.modificationDate = modificationDate;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
