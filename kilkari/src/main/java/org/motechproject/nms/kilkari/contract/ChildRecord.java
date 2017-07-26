package org.motechproject.nms.kilkari.contract;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType
@XmlAccessorType(XmlAccessType.NONE)
public class ChildRecord {

    private Long stateID;
    private Long districtId;
    private String districtName;
    private String talukaId;
    private String talukaName;
    private Long healthBlockId;
    private String healthBlockName;
    private Long phcId;
    private String phcName;
    private Long subCentreId;
    private String subCentreName;
    private Long villageId;
    private String villageName;
    private Integer yr;
    private String cityMaholla;
    private String gpVillage;
    private String address;
    private String idNo;
    private String name;
    private String motherName;
    private String motherId;
    private String phoneNoOfWhom;
    private String whomPhoneNo;
    private String birthdate;
    private String placeOfDelivery;
    private String bloodGroup;
    private String caste;
    private String subCentreName1;
    private String anmName;
    private String anmPhone;
    private String ashaName;
    private String ashaPhone;
    private String bcgDt;
    private String opv0Dt;
    private String hepatitisB1Dt;
    private String dpt1Dt;
    private String opv1Dt;
    private String hepatitisB2Dt;
    private String dPT2Dt;
    private String opv2Dt;
    private String hepatitisB3Dt;
    private String dpt3Dt;
    private String opv3Dt;
    private String hepatitisB4Dt;
    private String measlesDt;
    private String vitADose1Dt;
    private String mrDt;
    private String dptBoosterDt;
    private String opvBoosterDt;
    private String vitADose2Dt;
    private String vitADose3Dt;
    private String jeDt;
    private String vitADose9Dt;
    private String dt5Dt;
    private String tt10Dt;
    private String tt16Dt;
    private String cldRegDate;
    private String sex;
    private String vitADose5Dt;
    private String vitADose6Dt;
    private String vitADose7Dt;
    private String vitADose8Dt;
    private String lastUpdateDate;
    private String remarks;
    private Integer anmID;
    private Integer ashaID;
    private Integer createdBy;
    private Integer updatedBy;
    private String measles2Dt;
    private Double weightofChild;
    private Integer childAadhaarNo;
    private Integer childEID;
    private String childEIDTime;
    private String fatherName;
    private String birthCertificateNumber;
    private Integer entryType;

    public Long getStateID() {
        return stateID;
    }

    @XmlElement(name = "StateID")
    public void setStateID(Long stateID) {
        this.stateID = stateID;
    }

    public Long getDistrictId() {
        return districtId;
    }

    @XmlElement(name = "District_ID")
    public void setDistrictId(Long districtId) {
        this.districtId = districtId;
    }

    public String getDistrictName() {
        return districtName;
    }

    @XmlElement(name = "District_Name")
    public void setDistrictName(String districtName) {
        this.districtName = districtName;
    }

    public String getTalukaId() {
        return talukaId;
    }

    @XmlElement(name = "Taluka_ID")
    public void setTalukaId(String talukaId) {
        this.talukaId = talukaId;
    }

    public String getTalukaName() {
        return talukaName;
    }

    @XmlElement(name = "Taluka_Name")
    public void setTalukaName(String talukaName) {
        this.talukaName = talukaName;
    }

    public Long getHealthBlockId() {
        return healthBlockId;
    }

    @XmlElement(name = "HealthBlock_ID")
    public void setHealthBlockId(Long healthBlockId) {
        this.healthBlockId = healthBlockId;
    }

    public String getHealthBlockName() {
        return healthBlockName;
    }

    @XmlElement(name = "HealthBlock_Name")
    public void setHealthBlockName(String healthBlockName) {
        this.healthBlockName = healthBlockName;
    }

    public Long getPhcId() {
        return phcId;
    }

    @XmlElement(name = "PHC_ID")
    public void setPhcId(Long phcId) {
        this.phcId = phcId;
    }

    public String getPhcName() {
        return phcName;
    }

    @XmlElement(name = "PHC_Name")
    public void setPhcName(String phcName) {
        this.phcName = phcName;
    }

    public Long getSubCentreId() {
        return subCentreId;
    }

    @XmlElement(name = "SubCentre_ID")
    public void setSubCentreId(Long subCentreId) {
        this.subCentreId = subCentreId;
    }

    public String getSubCentreName() {
        return subCentreName;
    }

    @XmlElement(name = "SubCentre_Name")
    public void setSubCentreName(String subCentreName) {
        this.subCentreName = subCentreName;
    }

    public Long getVillageId() {
        return villageId;
    }

    @XmlElement(name = "Village_ID")
    public void setVillageId(Long villageId) {
        this.villageId = villageId;
    }

    public String getVillageName() {
        return villageName;
    }

    @XmlElement(name = "Village_Name")
    public void setVillageName(String villageName) {
        this.villageName = villageName;
    }

    public Integer getYr() {
        return yr;
    }

    @XmlElement(name = "Yr")
    public void setYr(Integer yr) {
        this.yr = yr;
    }

    public String getCityMaholla() {
        return cityMaholla;
    }

    @XmlElement(name = "City_Maholla")
    public void setCityMaholla(String cityMaholla) {
        this.cityMaholla = cityMaholla;
    }

    public String getGpVillage() {
        return gpVillage;
    }

    @XmlElement(name = "GP_Village")
    public void setGpVillage(String gpVillage) {
        this.gpVillage = gpVillage;
    }

    public String getAddress() {
        return address;
    }

    @XmlElement(name = "Address")
    public void setAddress(String address) {
        this.address = address;
    }

    public String getIdNo() {
        return idNo;
    }

    @XmlElement(name = "ID_No")
    public void setIdNo(String idNo) {
        this.idNo = idNo;
    }

    public String getName() {
        return name;
    }

    @XmlElement(name = "Name")
    public void setName(String name) {
        this.name = name;
    }

    public String getMotherName() {
        return motherName;
    }

    @XmlElement(name = "Mother_Name")
    public void setMotherName(String motherName) {
        this.motherName = motherName;
    }

    public String getMotherId() {
        return motherId;
    }

    @XmlElement(name = "Mother_ID")
    public void setMotherId(String motherId) {
        this.motherId = motherId;
    }

    public String getPhoneNoOfWhom() {
        return phoneNoOfWhom;
    }

    @XmlElement(name = "PhoneNo_Of_Whom")
    public void setPhoneNoOfWhom(String phoneNoOfWhom) {
        this.phoneNoOfWhom = phoneNoOfWhom;
    }

    public String getWhomPhoneNo() {
        return whomPhoneNo;
    }

    @XmlElement(name = "Whom_PhoneNo")
    public void setWhomPhoneNo(String whomPhoneNo) {
        this.whomPhoneNo = whomPhoneNo;
    }

    public String getBirthdate() {
        return birthdate;
    }

    @XmlElement(name = "Birthdate")
    public void setBirthdate(String birthdate) {
        this.birthdate = birthdate;
    }

    public String getPlaceOfDelivery() {
        return placeOfDelivery;
    }

    @XmlElement(name = "Place_of_Delivery")
    public void setPlaceOfDelivery(String placeOfDelivery) {
        this.placeOfDelivery = placeOfDelivery;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    @XmlElement(name = "Blood_Group")
    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public String getCaste() {
        return caste;
    }

    @XmlElement(name = "Caste")
    public void setCaste(String caste) {
        this.caste = caste;
    }

    public String getSubCentreName1() {
        return subCentreName1;
    }

    @XmlElement(name = "SubCentre_Name1")
    public void setSubCentreName1(String subCentreName1) {
        this.subCentreName1 = subCentreName1;
    }

    public String getAnmName() {
        return anmName;
    }

    @XmlElement(name = "ANM_Name")
    public void setAnmName(String anmName) {
        this.anmName = anmName;
    }

    public String getAnmPhone() {
        return anmPhone;
    }

    @XmlElement(name = "ANM_Phone")
    public void setAnmPhone(String anmPhone) {
        this.anmPhone = anmPhone;
    }

    public String getAshaName() {
        return ashaName;
    }

    @XmlElement(name = "ASHA_Name")
    public void setAshaName(String ashaName) {
        this.ashaName = ashaName;
    }

    public String getAshaPhone() {
        return ashaPhone;
    }

    @XmlElement(name = "ASHA_Phone")
    public void setAshaPhone(String ashaPhone) {
        this.ashaPhone = ashaPhone;
    }

    public String getBcgDt() {
        return bcgDt;
    }

    @XmlElement(name = "BCG_Dt")
    public void setBcgDt(String bcgDt) {
        this.bcgDt = bcgDt;
    }

    public String getOpv0Dt() {
        return opv0Dt;
    }

    @XmlElement(name = "OPV0_Dt")
    public void setOpv0Dt(String opv0Dt) {
        this.opv0Dt = opv0Dt;
    }

    public String getHepatitisB1Dt() {
        return hepatitisB1Dt;
    }

    @XmlElement(name = "HepatitisB1_Dt")
    public void setHepatitisB1Dt(String hepatitisB1Dt) {
        this.hepatitisB1Dt = hepatitisB1Dt;
    }

    public String getDpt1Dt() {
        return dpt1Dt;
    }

    @XmlElement(name = "DPT1_Dt")
    public void setDpt1Dt(String dpt1Dt) {
        this.dpt1Dt = dpt1Dt;
    }

    public String getOpv1Dt() {
        return opv1Dt;
    }

    @XmlElement(name = "OPV1_Dt")
    public void setOpv1Dt(String opv1Dt) {
        this.opv1Dt = opv1Dt;
    }

    public String getHepatitisB2Dt() {
        return hepatitisB2Dt;
    }

    @XmlElement(name = "HepatitisB2_Dt")
    public void setHepatitisB2Dt(String hepatitisB2Dt) {
        this.hepatitisB2Dt = hepatitisB2Dt;
    }

    public String getdPT2Dt() {
        return dPT2Dt;
    }

    @XmlElement(name = "DPT2_Dt")
    public void setdPT2Dt(String dPT2Dt) {
        this.dPT2Dt = dPT2Dt;
    }

    public String getOpv2Dt() {
        return opv2Dt;
    }

    @XmlElement(name = "OPV2_Dt")
    public void setOpv2Dt(String opv2Dt) {
        this.opv2Dt = opv2Dt;
    }

    public String getHepatitisB3Dt() {
        return hepatitisB3Dt;
    }

    @XmlElement(name = "HepatitisB3_Dt")
    public void setHepatitisB3Dt(String hepatitisB3Dt) {
        this.hepatitisB3Dt = hepatitisB3Dt;
    }

    public String getDpt3Dt() {
        return dpt3Dt;
    }

    @XmlElement(name = "DPT3_Dt")
    public void setDpt3Dt(String dpt3Dt) {
        this.dpt3Dt = dpt3Dt;
    }

    public String getOpv3Dt() {
        return opv3Dt;
    }

    @XmlElement(name = "OPV3_Dt")
    public void setOpv3Dt(String opv3Dt) {
        this.opv3Dt = opv3Dt;
    }

    public String getHepatitisB4Dt() {
        return hepatitisB4Dt;
    }

    @XmlElement(name = "HepatitisB4_Dt")
    public void setHepatitisB4Dt(String hepatitisB4Dt) {
        this.hepatitisB4Dt = hepatitisB4Dt;
    }

    public String getMeaslesDt() {
        return measlesDt;
    }

    @XmlElement(name = "Measles_Dt")
    public void setMeaslesDt(String measlesDt) {
        this.measlesDt = measlesDt;
    }

    public String getVitADose1Dt() {
        return vitADose1Dt;
    }

    @XmlElement(name = "VitA_Dose1_Dt")
    public void setVitADose1Dt(String vitADose1Dt) {
        this.vitADose1Dt = vitADose1Dt;
    }

    public String getMrDt() {
        return mrDt;
    }

    @XmlElement(name = "MR_Dt")
    public void setMrDt(String mrDt) {
        this.mrDt = mrDt;
    }

    public String getDptBoosterDt() {
        return dptBoosterDt;
    }

    @XmlElement(name = "DPTBooster_Dt")
    public void setDptBoosterDt(String dptBoosterDt) {
        this.dptBoosterDt = dptBoosterDt;
    }

    public String getOpvBoosterDt() {
        return opvBoosterDt;
    }

    @XmlElement(name = "OPVBooster_Dt")
    public void setOpvBoosterDt(String opvBoosterDt) {
        this.opvBoosterDt = opvBoosterDt;
    }

    public String getVitADose2Dt() {
        return vitADose2Dt;
    }

    @XmlElement(name = "VitA_Dose2_Dt")
    public void setVitADose2Dt(String vitADose2Dt) {
        this.vitADose2Dt = vitADose2Dt;
    }

    public String getVitADose3Dt() {
        return vitADose3Dt;
    }

    @XmlElement(name = "VitA_Dose3_Dt")
    public void setVitADose3Dt(String vitADose3Dt) {
        this.vitADose3Dt = vitADose3Dt;
    }

    public String getJeDt() {
        return jeDt;
    }

    @XmlElement(name = "JE_Dt")
    public void setJeDt(String jeDt) {
        this.jeDt = jeDt;
    }

    public String getVitADose9Dt() {
        return vitADose9Dt;
    }

    @XmlElement(name = "VitA_Dose9_Dt")
    public void setVitADose9Dt(String vitADose9Dt) {
        this.vitADose9Dt = vitADose9Dt;
    }

    public String getDt5Dt() {
        return dt5Dt;
    }

    @XmlElement(name = "DT5_Dt")
    public void setDt5Dt(String dt5Dt) {
        this.dt5Dt = dt5Dt;
    }

    public String getTt10Dt() {
        return tt10Dt;
    }

    @XmlElement(name = "TT10_Dt")
    public void setTt10Dt(String tt10Dt) {
        this.tt10Dt = tt10Dt;
    }

    public String getTt16Dt() {
        return tt16Dt;
    }

    @XmlElement(name = "TT16_Dt")
    public void setTt16Dt(String tt16Dt) {
        this.tt16Dt = tt16Dt;
    }

    public String getCldRegDate() {
        return cldRegDate;
    }

    @XmlElement(name = "CLD_REG_DATE")
    public void setCldRegDate(String cldRegDate) {
        this.cldRegDate = cldRegDate;
    }

    public String getSex() {
        return sex;
    }

    @XmlElement(name = "Sex")
    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getVitADose5Dt() {
        return vitADose5Dt;
    }

    @XmlElement(name = "VitA_Dose5_Dt")
    public void setVitADose5Dt(String vitADose5Dt) {
        this.vitADose5Dt = vitADose5Dt;
    }

    public String getVitADose6Dt() {
        return vitADose6Dt;
    }

    @XmlElement(name = "VitA_Dose6_Dt")
    public void setVitADose6Dt(String vitADose6Dt) {
        this.vitADose6Dt = vitADose6Dt;
    }

    public String getVitADose7Dt() {
        return vitADose7Dt;
    }

    @XmlElement(name = "VitA_Dose7_Dt")
    public void setVitADose7Dt(String vitADose7Dt) {
        this.vitADose7Dt = vitADose7Dt;
    }

    public String getVitADose8Dt() {
        return vitADose8Dt;
    }

    @XmlElement(name = "VitA_Dose8_Dt")
    public void setVitADose8Dt(String vitADose8Dt) {
        this.vitADose8Dt = vitADose8Dt;
    }

    public String getLastUpdateDate() {
        return lastUpdateDate;
    }

    @XmlElement(name = "LastUpdateDate")
    public void setLastUpdateDate(String lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public String getRemarks() {
        return remarks;
    }

    @XmlElement(name = "Remarks")
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public Integer getAnmID() {
        return anmID;
    }

    @XmlElement(name = "ANM_ID")
    public void setAnmID(Integer anmID) {
        this.anmID = anmID;
    }

    public Integer getAshaID() {
        return ashaID;
    }

    @XmlElement(name = "ASHA_ID")
    public void setAshaID(Integer ashaID) {
        this.ashaID = ashaID;
    }

    public Integer getCreatedBy() {
        return createdBy;
    }

    @XmlElement(name = "Created_By")
    public void setCreatedBy(Integer createdBy) {
        this.createdBy = createdBy;
    }

    public Integer getUpdatedBy() {
        return updatedBy;
    }

    @XmlElement(name = "Updated_By")
    public void setUpdatedBy(Integer updatedBy) {
        this.updatedBy = updatedBy;
    }

    public String getMeasles2Dt() {
        return measles2Dt;
    }

    @XmlElement(name = "Measles2_Dt")
    public void setMeasles2Dt(String measles2Dt) {
        this.measles2Dt = measles2Dt;
    }

    public Double getWeightofChild() {
        return weightofChild;
    }

    @XmlElement(name = "Weight_of_Child")
    public void setWeightofChild(Double weightofChild) {
        this.weightofChild = weightofChild;
    }

    public Integer getChildAadhaarNo() {
        return childAadhaarNo;
    }

    @XmlElement(name = "Child_Aadhaar_No")
    public void setChildAadhaarNo(Integer childAadhaarNo) {
        this.childAadhaarNo = childAadhaarNo;
    }

    public Integer getChildEID() {
        return childEID;
    }

    @XmlElement(name = "Child_EID")
    public void setChildEID(Integer childEID) {
        this.childEID = childEID;
    }

    public String getChildEIDTime() {
        return childEIDTime;
    }

    @XmlElement(name = "Child_EIDTime")
    public void setChildEIDTime(String childEIDTime) {
        this.childEIDTime = childEIDTime;
    }

    public String getFatherName() {
        return fatherName;
    }

    @XmlElement(name = "Father_Name")
    public void setFatherName(String fatherName) {
        this.fatherName = fatherName;
    }

    public String getBirthCertificateNumber() {
        return birthCertificateNumber;
    }

    @XmlElement(name = "Birth_Certificate_Number")
    public void setBirthCertificateNumber(String birthCertificateNumber) {
        this.birthCertificateNumber = birthCertificateNumber;
    }

    public Integer getEntryType() {
        return entryType;
    }

    @XmlElement(name = "Entry_Type")
    public void setEntryType(Integer entryType) {
        this.entryType = entryType;
    }
}
