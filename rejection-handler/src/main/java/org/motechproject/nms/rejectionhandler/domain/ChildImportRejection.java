package org.motechproject.nms.rejectionhandler.domain;

import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;

@Entity(tableName = "nms_child_rejects")
public class ChildImportRejection {

    @Field
    private Long stateId;

    @Field
    private Long districtId;

    @Field
    private String districtName;

    @Field
    private Long phcId;

    @Field
    private String phcName;

    @Field
    private Long subcentreId;

    @Field
    private String talukaId;

    @Field
    private String talukaName;

    @Field
    private Long healthBlockId;

    @Field
    private String healthBlockName;

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
    private String gPVillage;

    @Field
    private String address;

    @Field
    private String idNo;

    @Field
    private String name;

    @Field
    private String mobileNo;

//    @Field
//    private String rchAshaId;


    @Field
    private String motherName;

    @Field
    private String rCHMotherIDNo;

    @Field
    private String ashaPhone;

    @Field
    private String bCGDt;

    @Field
    private String oPV0Dt;

    @Field
    private String hepatitisB1Dt;

    @Field
    private String dPT1Dt;

    @Field
    private String oPV1Dt;

    @Field
    private String hepatitisB2Dt;

    @Field
    private String phoneNumberWhom;

    @Field
    private String birthDate;

    @Field
    private String registrationDate;

    @Field
    private String placeOfDelivery;

    @Field
    private String bloodGroup;

    @Field
    private String caste;

    @Field
    private String subcenterName1;

    @Field
    private String aNMName;

    @Field
    private String aNMPhone;

    @Field
    private String ashaName;

    @Field
    private String dPT2Dt;

    @Field
    private String oPV2Dt;

    @Field
    private String hepatitisB3Dt;

    @Field
    private String dPT3Dt;

    @Field
    private String oPV3Dt;

    @Field
    private String hepatitisB4Dt;

    @Field
    private String measlesDt;

    @Field
    private String vitADose1Dt;

    @Field
    private String mRDt;

    @Field
    private String dPTBoosterDt;

    @Field
    private String oPVBoosterDt;

    @Field
    private String vitADose2Dt;

    @Field
    private String vitADose3Dt;

    @Field
    private String tT10Dt;

    @Field
    private String jEDt;

    @Field
    private String vitADose9Dt;

    @Field
    private String dT5Dt;

    @Field
    private String tT16Dt;

    @Field
    private String cLDRegDATE;

    @Field
    private Integer ashaID;

    @Field
    private String lastUpdateDate;

    @Field
    private String vitADose6Dt;

    @Field
    private String remarks;

    @Field
    private Integer aNMID;

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
    private String sex;

    @Field
    private String vitADose5Dt;

    @Field
    private String vitADose7Dt;

    @Field
    private String vitADose8Dt;

    @Field
    private String childEIDTime;

    @Field
    private String fatherName;

    @Field
    private String execDate;

    @Field
    private Boolean accepted;

    @Field
    private String rejectionReason;

    @Field
    private String birthCertificateNumber;

    @Field
    private String entryType;

    @Field
    private String source;

    @Field
    private String registrationNo;

    @Field
    private String mCTSMotherIDNo;

    @Field
    private String action;

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

    public String getgPVillage() {
        return gPVillage;
    }

    public void setgPVillage(String gPVillage) {
        this.gPVillage = gPVillage;
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

//    public String getRchAshaId() {return rchAshaId;}
//
//    public void setRchAshaId(String rchAshaId) {this.rchAshaId = rchAshaId;}

    public String getMotherName() {
        return motherName;
    }

    public void setMotherName(String motherName) {
        this.motherName = motherName;
    }

    public String getMotherId() {
        return rCHMotherIDNo;
    }

    public void setMotherId(String motherId) {
        this.rCHMotherIDNo = motherId;
    }

    public String getAshaPhone() {
        return ashaPhone;
    }

    public void setAshaPhone(String ashaPhone) {
        this.ashaPhone = ashaPhone;
    }

    public String getbCGDt() {
        return bCGDt;
    }

    public void setbCGDt(String bCGDt) {
        this.bCGDt = bCGDt;
    }

    public String getoPV0Dt() {
        return oPV0Dt;
    }

    public void setoPV0Dt(String oPV0Dt) {
        this.oPV0Dt = oPV0Dt;
    }

    public String getHepatitisB1Dt() {
        return hepatitisB1Dt;
    }

    public void setHepatitisB1Dt(String hepatitisB1Dt) {
        this.hepatitisB1Dt = hepatitisB1Dt;
    }

    public String getdPT1Dt() {
        return dPT1Dt;
    }

    public void setdPT1Dt(String dPT1Dt) {
        this.dPT1Dt = dPT1Dt;
    }

    public String getoPV1Dt() {
        return oPV1Dt;
    }

    public void setoPV1Dt(String oPV1Dt) {
        this.oPV1Dt = oPV1Dt;
    }

    public String getHepatitisB2Dt() {
        return hepatitisB2Dt;
    }

    public void setHepatitisB2Dt(String hepatitisB2Dt) {
        this.hepatitisB2Dt = hepatitisB2Dt;
    }

    public String getPhoneNumberWhom() {
        return phoneNumberWhom;
    }

    public void setPhoneNumberWhom(String phoneNumberWhom) {
        this.phoneNumberWhom = phoneNumberWhom;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public String getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(String registrationDate) {
        this.registrationDate = registrationDate;
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

    public void setaNMName(String aNMName) {
        this.aNMName = aNMName;
    }

    public String getaNMPhone() {
        return aNMPhone;
    }

    public void setaNMPhone(String aNMPhone) {
        this.aNMPhone = aNMPhone;
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

    public String getaNMName() {
        return aNMName;
    }

    public String getAshaName() {
        return ashaName;
    }

    public void setAshaName(String ashaName) {
        this.ashaName = ashaName;
    }

    public String getdPT2Dt() {
        return dPT2Dt;
    }

    public void setdPT2Dt(String dPT2Dt) {
        this.dPT2Dt = dPT2Dt;
    }

    public String getoPV2Dt() {
        return oPV2Dt;
    }

    public void setoPV2Dt(String oPV2Dt) {
        this.oPV2Dt = oPV2Dt;
    }

    public String getHepatitisB3Dt() {
        return hepatitisB3Dt;
    }

    public void setHepatitisB3Dt(String hepatitisB3Dt) {
        this.hepatitisB3Dt = hepatitisB3Dt;
    }

    public String getdPT3Dt() {
        return dPT3Dt;
    }

    public void setdPT3Dt(String dPT3Dt) {
        this.dPT3Dt = dPT3Dt;
    }

    public String getoPV3Dt() {
        return oPV3Dt;
    }

    public void setoPV3Dt(String oPV3Dt) {
        this.oPV3Dt = oPV3Dt;
    }

    public String getHepatitisB4Dt() {
        return hepatitisB4Dt;
    }

    public void setHepatitisB4Dt(String hepatitisB4Dt) {
        this.hepatitisB4Dt = hepatitisB4Dt;
    }

    public String getMeaslesDt() {
        return measlesDt;
    }

    public void setMeaslesDt(String measlesDt) {
        this.measlesDt = measlesDt;
    }

    public String getVitADose1Dt() {
        return vitADose1Dt;
    }

    public void setVitADose1Dt(String vitADose1Dt) {
        this.vitADose1Dt = vitADose1Dt;
    }

    public String getmRDt() {
        return mRDt;
    }

    public void setmRDt(String mRDt) {
        this.mRDt = mRDt;
    }

    public String getdPTBoosterDt() {
        return dPTBoosterDt;
    }

    public void setdPTBoosterDt(String dPTBoosterDt) {
        this.dPTBoosterDt = dPTBoosterDt;
    }

    public String getoPVBoosterDt() {
        return oPVBoosterDt;
    }

    public void setoPVBoosterDt(String oPVBoosterDt) {
        this.oPVBoosterDt = oPVBoosterDt;
    }

    public String getVitADose2Dt() {
        return vitADose2Dt;
    }

    public void setVitADose2Dt(String vitADose2Dt) {
        this.vitADose2Dt = vitADose2Dt;
    }

    public String getVitADose3Dt() {
        return vitADose3Dt;
    }

    public void setVitADose3Dt(String vitADose3Dt) {
        this.vitADose3Dt = vitADose3Dt;
    }

    public String gettT10Dt() {
        return tT10Dt;
    }

    public void settT10Dt(String tT10Dt) {
        this.tT10Dt = tT10Dt;
    }

    public String getjEDt() {
        return jEDt;
    }

    public void setjEDt(String jEDt) {
        this.jEDt = jEDt;
    }

    public String getVitADose9Dt() {
        return vitADose9Dt;
    }

    public void setVitADose9Dt(String vitADose9Dt) {
        this.vitADose9Dt = vitADose9Dt;
    }

    public String getdT5Dt() {
        return dT5Dt;
    }

    public void setdT5Dt(String dT5Dt) {
        this.dT5Dt = dT5Dt;
    }

    public String gettT16Dt() {
        return tT16Dt;
    }

    public void settT16Dt(String tT16Dt) {
        this.tT16Dt = tT16Dt;
    }

    public String getcLDRegDATE() {
        return cLDRegDATE;
    }

    public void setcLDRegDATE(String cLDRegDATE) {
        this.cLDRegDATE = cLDRegDATE;
    }

    public Integer getAshaID() {
        return ashaID;
    }

    public void setAshaID(Integer ashaID) {
        this.ashaID = ashaID;
    }

    public String getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(String lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public String getVitADose6Dt() {
        return vitADose6Dt;
    }

    public void setVitADose6Dt(String vitADose6Dt) {
        this.vitADose6Dt = vitADose6Dt;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public Integer getaNMID() {
        return aNMID;
    }

    public void setaNMID(Integer aNMID) {
        this.aNMID = aNMID;
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

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getVitADose5Dt() {
        return vitADose5Dt;
    }

    public void setVitADose5Dt(String vitADose5Dt) {
        this.vitADose5Dt = vitADose5Dt;
    }

    public String getVitADose7Dt() {
        return vitADose7Dt;
    }

    public void setVitADose7Dt(String vitADose7Dt) {
        this.vitADose7Dt = vitADose7Dt;
    }

    public String getVitADose8Dt() {
        return vitADose8Dt;
    }

    public void setVitADose8Dt(String vitADose8Dt) {
        this.vitADose8Dt = vitADose8Dt;
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

    public String getExecDate() {
        return execDate;
    }

    public void setExecDate(String execDate) {
        this.execDate = execDate;
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

    public String getBirthCertificateNumber() {
        return birthCertificateNumber;
    }

    public void setBirthCertificateNumber(String birthCertificateNumber) {
        this.birthCertificateNumber = birthCertificateNumber;
    }

    public String getEntryType() {
        return entryType;
    }

    public void setEntryType(String entryType) {
        this.entryType = entryType;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getRegistrationNo() {
        return registrationNo;
    }

    public void setRegistrationNo(String registrationNo) {
        this.registrationNo = registrationNo;
    }

    public String getmCTSMotherIDNo() {
        return mCTSMotherIDNo;
    }

    public void setmCTSMotherIDNo(String mCTSMotherIDNo) {
        this.mCTSMotherIDNo = mCTSMotherIDNo;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
