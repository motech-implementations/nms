package org.motechproject.nms.kilkari.contract;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType
@XmlAccessorType(XmlAccessType.NONE)
public class MotherRecord {

    private Long stateId;
    private Long districtId;
    private String districtName;
    private String talukaId;
    private String talukaName;
    private Long healthBlockId;
    private String healthBlockName;
    private Long phcid;
    private String phcName;
    private Long subCentreid;
    private String subCentreName;
    private Long villageId;
    private String villageName;
    private Integer yr;
    private String gpVillage;
    private String address;
    private String idNo;
    private String name;
    private String husbandName;
    private String phoneNoOfWhom;
    private String whomPhoneNo;
    private String birthdate;
    private String jsyBeneficiary;
    private String caste;
    private String subCentreName1;
    private String anmName;
    private String anmPhone;
    private String ashaName;
    private String ashaPhone;
    private String deliveryLnkFacility;
    private String facilityName;
    private String lmpDate;
    private String anc1Date;
    private String anc2Date;
    private String anc3Date;
    private String anc4Date;
    private String tt1Date;
    private String tt2Date;
    private String ttBoosterDate;
    private String ifA100GivenDate;
    private String anemia;
    private String ancComplication;
    private String rtiSTI;
    private String dlyDate;
    private String dlyPlaceHomeType;
    private String dlyPlacePublic;
    private String dlyPlacePrivate;
    private String dlyType;
    private String dlyComplication;
    private String dischargeDate;
    private String jsyPaidDate;
    private String abortion;
    private String pncHomeVisit;
    private String pncComplication;
    private String ppcMethod;
    private String pncCheckup;
    private Integer outcomeNos;
    private String child1Name;
    private String child1Sex;
    private Double child1Wt;
    private String child1Brestfeeding;
    private String child2Name;
    private String child2Sex;
    private Double child2Wt;
    private String child2Brestfeeding;
    private String child3Name;
    private String child3Sex;
    private Double child3Wt;
    private String child3Brestfeeding;
    private String child4Name;
    private String child4Sex;
    private Double child4Wt;
    private String child4Brestfeeding;
    private Integer age;
    private String mthrRegDate;
    private String lastUpdateDate;
    private String remarks;
    private Integer anmID;
    private Integer ashaID;
    private Boolean callAns;
    private Integer noCallReason;
    private Integer noPhoneReason;
    private Integer createdBy;
    private Integer updatedBy;
    private Integer aadharNo;
    private Integer bplAPL;
    private Integer eId;
    private String eIdTime;
    private Integer entryType;

    private String motherRegistrationDate;
    private String asha_Id;
    @XmlElement(name = "HealthBlock_Name")
    public void setHealthBlockName(String healthBlockName) {
        this.healthBlockName = healthBlockName;
    }

    public Long getPhcid() {
        return phcid;
    }

    @XmlElement(name = "PHC_ID")
    public void setPhcid(Long phcid) {
        this.phcid = phcid;
    }

    public String getPhcName() {
        return phcName;
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

    public Long getStateId() {
        return stateId;
    }

    @XmlElement(name = "StateID")
    public void setStateId(Long stateId) {
        this.stateId = stateId;
    }

    public Long getDistrictId() {
        return districtId;
    }

    @XmlElement(name = "PHC_Name")
    public void setPhcName(String phcName) {
        this.phcName = phcName;
    }

    public Long getSubCentreid() {
        return subCentreid;
    }

    @XmlElement(name = "SubCentre_ID")
    public void setSubCentreid(Long subCentreid) {
        this.subCentreid = subCentreid;
    }

    public String getSubCentreName() {
        return subCentreName;
    }

    @XmlElement(name = "SubCentre_Name")
    public void setSubCentreName(String subCentreName) {
        this.subCentreName = subCentreName;
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

    public String getHusbandName() {
        return husbandName;
    }

    @XmlElement(name = "Husband_Name")
    public void setHusbandName(String husbandName) {
        this.husbandName = husbandName;
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

    public String getJsyBeneficiary() {
        return jsyBeneficiary;
    }

    @XmlElement(name = "JSY_Beneficiary")
    public void setJsyBeneficiary(String jsyBeneficiary) {
        this.jsyBeneficiary = jsyBeneficiary;
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

    public String getDeliveryLnkFacility() {
        return deliveryLnkFacility;
    }

    @XmlElement(name = "Delivery_Lnk_Facility")
    public void setDeliveryLnkFacility(String deliveryLnkFacility) {
        this.deliveryLnkFacility = deliveryLnkFacility;
    }

    public String getFacilityName() {
        return facilityName;
    }

    @XmlElement(name = "Facility_Name")
    public void setFacilityName(String facilityName) {
        this.facilityName = facilityName;
    }

    public String getLmpDate() {
        return lmpDate;
    }

    @XmlElement(name = "LMP_Date")
    public void setLmpDate(String lmpDate) {
        this.lmpDate = lmpDate;
    }

    public String getAnc1Date() {
        return anc1Date;
    }

    @XmlElement(name = "ANC1_Date")
    public void setAnc1Date(String anc1Date) {
        this.anc1Date = anc1Date;
    }

    public String getAnc2Date() {
        return anc2Date;
    }

    @XmlElement(name = "ANC2_Date")
    public void setAnc2Date(String anc2Date) {
        this.anc2Date = anc2Date;
    }

    public String getAnc3Date() {
        return anc3Date;
    }

    @XmlElement(name = "ANC3_Date")
    public void setAnc3Date(String anc3Date) {
        this.anc3Date = anc3Date;
    }

    public String getAnc4Date() {
        return anc4Date;
    }

    @XmlElement(name = "ANC4_Date")
    public void setAnc4Date(String anc4Date) {
        this.anc4Date = anc4Date;
    }

    public String getTt1Date() {
        return tt1Date;
    }

    @XmlElement(name = "TT1_Date")
    public void setTt1Date(String tt1Date) {
        this.tt1Date = tt1Date;
    }

    public String getTt2Date() {
        return tt2Date;
    }

    @XmlElement(name = "TT2_Date")
    public void setTt2Date(String tt2Date) {
        this.tt2Date = tt2Date;
    }

    public String getTtBoosterDate() {
        return ttBoosterDate;
    }

    @XmlElement(name = "TTBooster_Date")
    public void setTtBoosterDate(String ttBoosterDate) {
        this.ttBoosterDate = ttBoosterDate;
    }

    public String getIfA100GivenDate() {
        return ifA100GivenDate;
    }

    @XmlElement(name = "IFA100_Given_Date")
    public void setIfA100GivenDate(String ifA100GivenDate) {
        this.ifA100GivenDate = ifA100GivenDate;
    }

    public String getAnemia() {
        return anemia;
    }

    @XmlElement(name = "Anemia")
    public void setAnemia(String anemia) {
        this.anemia = anemia;
    }

    public String getAncComplication() {
        return ancComplication;
    }

    @XmlElement(name = "ANC_Complication")
    public void setAncComplication(String ancComplication) {
        this.ancComplication = ancComplication;
    }

    public String getRtiSTI() {
        return rtiSTI;
    }

    @XmlElement(name = "RTI_STI")
    public void setRtiSTI(String rtiSTI) {
        this.rtiSTI = rtiSTI;
    }

    public String getDlyDate() {
        return dlyDate;
    }

    @XmlElement(name = "Dly_Date")
    public void setDlyDate(String dlyDate) {
        this.dlyDate = dlyDate;
    }

    public String getDlyPlaceHomeType() {
        return dlyPlaceHomeType;
    }

    @XmlElement(name = "Dly_Place_Home_Type")
    public void setDlyPlaceHomeType(String dlyPlaceHomeType) {
        this.dlyPlaceHomeType = dlyPlaceHomeType;
    }

    public String getDlyPlacePublic() {
        return dlyPlacePublic;
    }

    @XmlElement(name = "Dly_Place_Public")
    public void setDlyPlacePublic(String dlyPlacePublic) {
        this.dlyPlacePublic = dlyPlacePublic;
    }

    public String getDlyPlacePrivate() {
        return dlyPlacePrivate;
    }

    @XmlElement(name = "Dly_Place_Private")
    public void setDlyPlacePrivate(String dlyPlacePrivate) {
        this.dlyPlacePrivate = dlyPlacePrivate;
    }

    public String getDlyType() {
        return dlyType;
    }

    @XmlElement(name = "Dly_Type")
    public void setDlyType(String dlyType) {
        this.dlyType = dlyType;
    }

    public String getDlyComplication() {
        return dlyComplication;
    }

    @XmlElement(name = "Dly_Complication")
    public void setDlyComplication(String dlyComplication) {
        this.dlyComplication = dlyComplication;
    }

    public String getDischargeDate() {
        return dischargeDate;
    }

    @XmlElement(name = "Discharge_Date")
    public void setDischargeDate(String dischargeDate) {
        this.dischargeDate = dischargeDate;
    }

    public String getJsyPaidDate() {
        return jsyPaidDate;
    }

    @XmlElement(name = "JSY_Paid_Date")
    public void setJsyPaidDate(String jsyPaidDate) {
        this.jsyPaidDate = jsyPaidDate;
    }

    public String getAbortion() {
        return abortion;
    }

    @XmlElement(name = "Abortion")
    public void setAbortion(String abortion) {
        this.abortion = abortion;
    }

    public String getPncHomeVisit() {
        return pncHomeVisit;
    }

    @XmlElement(name = "PNC_Home_Visit")
    public void setPncHomeVisit(String pncHomeVisit) {
        this.pncHomeVisit = pncHomeVisit;
    }

    public String getPncComplication() {
        return pncComplication;
    }

    @XmlElement(name = "PNC_Complication")
    public void setPncComplication(String pncComplication) {
        this.pncComplication = pncComplication;
    }

    public String getPpcMethod() {
        return ppcMethod;
    }

    @XmlElement(name = "PPC_Method")
    public void setPpcMethod(String ppcMethod) {
        this.ppcMethod = ppcMethod;
    }

    public String getPncCheckup() {
        return pncCheckup;
    }

    @XmlElement(name = "PNC_Checkup")
    public void setPncCheckup(String pncCheckup) {
        this.pncCheckup = pncCheckup;
    }

    public Integer getOutcomeNos() {
        return outcomeNos;
    }

    @XmlElement(name = "Outcome_Nos")
    public void setOutcomeNos(Integer outcomeNos) {
        this.outcomeNos = outcomeNos;
    }

    public String getChild1Name() {
        return child1Name;
    }

    @XmlElement(name = "Child1_Name")
    public void setChild1Name(String child1Name) {
        this.child1Name = child1Name;
    }

    public String getChild1Sex() {
        return child1Sex;
    }

    @XmlElement(name = "Child1_Sex")
    public void setChild1Sex(String child1Sex) {
        this.child1Sex = child1Sex;
    }

    public Double getChild1Wt() {
        return child1Wt;
    }

    @XmlElement(name = "Child1_Wt")
    public void setChild1Wt(Double child1Wt) {
        this.child1Wt = child1Wt;
    }

    public String getChild1Brestfeeding() {
        return child1Brestfeeding;
    }

    @XmlElement(name = "Child1_Brestfeeding")
    public void setChild1Brestfeeding(String child1Brestfeeding) {
        this.child1Brestfeeding = child1Brestfeeding;
    }

    public String getChild2Name() {
        return child2Name;
    }

    @XmlElement(name = "Child2_Name")
    public void setChild2Name(String child2Name) {
        this.child2Name = child2Name;
    }

    public String getChild2Sex() {
        return child2Sex;
    }

    @XmlElement(name = "Child2_Sex")
    public void setChild2Sex(String child2Sex) {
        this.child2Sex = child2Sex;
    }

    public Double getChild2Wt() {
        return child2Wt;
    }

    @XmlElement(name = "Child2_Wt")
    public void setChild2Wt(Double child2Wt) {
        this.child2Wt = child2Wt;
    }

    public String getChild2Brestfeeding() {
        return child2Brestfeeding;
    }

    @XmlElement(name = "Child2_Brestfeeding")
    public void setChild2Brestfeeding(String child2Brestfeeding) {
        this.child2Brestfeeding = child2Brestfeeding;
    }

    public String getChild3Name() {
        return child3Name;
    }

    @XmlElement(name = "Child3_Name")
    public void setChild3Name(String child3Name) {
        this.child3Name = child3Name;
    }

    public String getChild3Sex() {
        return child3Sex;
    }

    @XmlElement(name = "Child3_Sex")
    public void setChild3Sex(String child3Sex) {
        this.child3Sex = child3Sex;
    }

    public Double getChild3Wt() {
        return child3Wt;
    }

    @XmlElement(name = "Child3_Wt")
    public void setChild3Wt(Double child3Wt) {
        this.child3Wt = child3Wt;
    }

    public String getChild3Brestfeeding() {
        return child3Brestfeeding;
    }

    @XmlElement(name = "Child3_Brestfeeding")
    public void setChild3Brestfeeding(String child3Brestfeeding) {
        this.child3Brestfeeding = child3Brestfeeding;
    }

    public String getChild4Name() {
        return child4Name;
    }

    @XmlElement(name = "Child4_Name")
    public void setChild4Name(String child4Name) {
        this.child4Name = child4Name;
    }

    public String getChild4Sex() {
        return child4Sex;
    }

    @XmlElement(name = "Child4_Sex")
    public void setChild4Sex(String child4Sex) {
        this.child4Sex = child4Sex;
    }

    public Double getChild4Wt() {
        return child4Wt;
    }

    @XmlElement(name = "Child4_Wt")
    public void setChild4Wt(Double child4Wt) {
        this.child4Wt = child4Wt;
    }

    public String getChild4Brestfeeding() {
        return child4Brestfeeding;
    }

    @XmlElement(name = "Child4_Brestfeeding")
    public void setChild4Brestfeeding(String child4Brestfeeding) {
        this.child4Brestfeeding = child4Brestfeeding;
    }

    public Integer getAge() {
        return age;
    }

    @XmlElement(name = "Age")
    public void setAge(Integer age) {
        this.age = age;
    }

    public String getMthrRegDate() {
        return mthrRegDate;
    }

    @XmlElement(name = "MTHR_REG_DATE")
    public void setMthrRegDate(String mthrRegDate) {
        this.mthrRegDate = mthrRegDate;
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

    public Boolean getCallAns() {
        return callAns;
    }

    @XmlElement(name = "Call_Ans")
    public void setCallAns(Boolean callAns) {
        this.callAns = callAns;
    }

    public Integer getNoCallReason() {
        return noCallReason;
    }

    @XmlElement(name = "NoCall_Reason")
    public void setNoCallReason(Integer noCallReason) {
        this.noCallReason = noCallReason;
    }

    public Integer getNoPhoneReason() {
        return noPhoneReason;
    }

    @XmlElement(name = "NoPhone_Reason")
    public void setNoPhoneReason(Integer noPhoneReason) {
        this.noPhoneReason = noPhoneReason;
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

    public Integer getAadharNo() {
        return aadharNo;
    }

    @XmlElement(name = "Aadhar_No")
    public void setAadharNo(Integer aadharNo) {
        this.aadharNo = aadharNo;
    }

    public Integer getBplAPL() {
        return bplAPL;
    }

    @XmlElement(name = "BPL_APL")
    public void setBplAPL(Integer bplAPL) {
        this.bplAPL = bplAPL;
    }

    public Integer geteId() {
        return eId;
    }

    @XmlElement(name = "EID")
    public void seteId(Integer eId) {
        this.eId = eId;
    }

    public String geteIdTime() {
        return eIdTime;
    }

    @XmlElement(name = "EIDTime")
    public void seteIdTime(String eIdTime) {
        this.eIdTime = eIdTime;
    }

    public Integer getEntryType() {
        return entryType;
    }

    @XmlElement(name = "Entry_Type")
    public void setEntryType(Integer entryType) {
        this.entryType = entryType;
    }

    public String getMotherRegistrationDate() {
        return motherRegistrationDate;
    }

    @XmlElement(name = "Mother_Registration_Date")
    public void setMotherRegistrationDate(String motherRegistrationDate) {
        this.motherRegistrationDate = motherRegistrationDate;
    }

    public String getAsha_Id() {
        return asha_Id;
    }

    @XmlElement(name = "Asha_ID")
    public void setAsha_Id(String asha_Id) {
        this.asha_Id = asha_Id;
    }
}


