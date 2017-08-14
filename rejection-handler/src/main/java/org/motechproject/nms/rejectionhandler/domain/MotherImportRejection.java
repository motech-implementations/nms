package org.motechproject.nms.rejectionhandler.domain;

import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;

@Entity(tableName = "nms_mother_rejects")
public class MotherImportRejection {

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
    private String gPVillage;

    @Field
    private String address;

    @Field
    private String idNo;

    @Field
    private String name;

    @Field
    private String husbandName;

    @Field
    private String phoneNumberWhom;

    @Field
    private String birthDate;

    @Field
    private String jSYBeneficiary;

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
    private String ashaPhone;

    @Field
    private String deliveryLnkFacility;

    @Field
    private String facilityName;

    @Field
    private String lmpDate;

    @Field
    private String aNC1Date;

    @Field
    private String aNC2Date;

    @Field
    private String aNC3Date;

    @Field
    private String aNC4Date;

    @Field
    private String tT1Date;

    @Field
    private String tT2Date;

    @Field
    private String tTBoosterDate;

    @Field
    private String iFA100GivenDate;

    @Field
    private String anemia;

    @Field
    private String aNCComplication;

    @Field
    private String rTISTI;

    @Field
    private String dlyDate;

    @Field
    private String dlyPlaceHomeType;

    @Field
    private String dlyPlacePublic;

    @Field
    private String dlyPlacePrivate;

    @Field
    private String dlyType;

    @Field
    private String dlyComplication;

    @Field
    private String dischargeDate;

    @Field
    private String jSYPaidDate;

    @Field
    private String abortion;

    @Field
    private String pNCHomeVisit;

    @Field
    private String pNCComplication;

    @Field
    private String pPCMethod;

    @Field
    private String pNCCheckup;

    @Field
    private Integer outcomeNos;

    @Field
    private String child1Name;

    @Field
    private String child1Sex;

    @Field
    private Double child1Wt;

    @Field
    private String child1Brestfeeding;

    @Field
    private String child2Name;

    @Field
    private String child2Sex;

    @Field
    private Double child2Wt;

    @Field
    private String child2Brestfeeding;

    @Field
    private String child3Name;

    @Field
    private String child3Sex;

    @Field
    private Double child3Wt;

    @Field
    private String child3Brestfeeding;

    @Field
    private String child4Name;

    @Field
    private String child4Sex;

    @Field
    private Double child4Wt;

    @Field
    private String child4Brestfeeding;

    @Field
    private Integer age;

    @Field
    private String mTHRREGDATE;

    @Field
    private String lastUpdateDate;

    @Field
    private String remarks;

    @Field
    private Integer aNMID;

    @Field
    private Integer aSHAID;

    @Field
    private Boolean callAns;

    @Field
    private Integer noCallReason;

    @Field
    private Integer noPhoneReason;

    @Field
    private Integer createdBy;

    @Field
    private Integer updatedBy;

    @Field
    private Integer aadharNo;

    @Field
    private Integer bPLAPL;

    @Field
    private Integer eID;

    @Field
    private String eIDTime;

    @Field
    private Integer entryType;

    @Field
    private String registrationNo;

    @Field
    private Long caseNo;

    @Field
    private String mobileNo;

    @Field
    private String abortionType;

    @Field
    private String deliveryOutcomes;

    @Field
    private String execDate;

    @Field
    private Boolean accepted;

    @Field
    private String rejectionReason;

    @Field
    private String source;

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

    public String getHusbandName() {
        return husbandName;
    }

    public void setHusbandName(String husbandName) {
        this.husbandName = husbandName;
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

    public String getjSYBeneficiary() {
        return jSYBeneficiary;
    }

    public void setjSYBeneficiary(String jSYBeneficiary) {
        this.jSYBeneficiary = jSYBeneficiary;
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

    public void setaNMName(String aNMName) {
        this.aNMName = aNMName;
    }

    public String getaNMPhone() {
        return aNMPhone;
    }

    public void setaNMPhone(String aNMPhone) {
        this.aNMPhone = aNMPhone;
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

    public String getDeliveryLnkFacility() {
        return deliveryLnkFacility;
    }

    public void setDeliveryLnkFacility(String deliveryLnkFacility) {
        this.deliveryLnkFacility = deliveryLnkFacility;
    }

    public String getFacilityName() {
        return facilityName;
    }

    public void setFacilityName(String facilityName) {
        this.facilityName = facilityName;
    }

    public String getLmpDate() {
        return lmpDate;
    }

    public void setLmpDate(String lmpDate) {
        this.lmpDate = lmpDate;
    }

    public String getaNC1Date() {
        return aNC1Date;
    }

    public void setaNC1Date(String aNC1Date) {
        this.aNC1Date = aNC1Date;
    }

    public String getaNC2Date() {
        return aNC2Date;
    }

    public void setaNC2Date(String aNC2Date) {
        this.aNC2Date = aNC2Date;
    }

    public String getaNC3Date() {
        return aNC3Date;
    }

    public void setaNC3Date(String aNC3Date) {
        this.aNC3Date = aNC3Date;
    }

    public String getaNC4Date() {
        return aNC4Date;
    }

    public void setaNC4Date(String aNC4Date) {
        this.aNC4Date = aNC4Date;
    }

    public String gettT1Date() {
        return tT1Date;
    }

    public void settT1Date(String tT1Date) {
        this.tT1Date = tT1Date;
    }

    public String gettT2Date() {
        return tT2Date;
    }

    public void settT2Date(String tT2Date) {
        this.tT2Date = tT2Date;
    }

    public String gettTBoosterDate() {
        return tTBoosterDate;
    }

    public void settTBoosterDate(String tTBoosterDate) {
        this.tTBoosterDate = tTBoosterDate;
    }

    public String getiFA100GivenDate() {
        return iFA100GivenDate;
    }

    public void setiFA100GivenDate(String iFA100GivenDate) {
        this.iFA100GivenDate = iFA100GivenDate;
    }

    public String getAnemia() {
        return anemia;
    }

    public void setAnemia(String anemia) {
        this.anemia = anemia;
    }

    public String getaNCComplication() {
        return aNCComplication;
    }

    public void setaNCComplication(String aNCComplication) {
        this.aNCComplication = aNCComplication;
    }

    public String getrTISTI() {
        return rTISTI;
    }

    public void setrTISTI(String rTISTI) {
        this.rTISTI = rTISTI;
    }

    public String getDlyDate() {
        return dlyDate;
    }

    public void setDlyDate(String dlyDate) {
        this.dlyDate = dlyDate;
    }

    public String getDlyPlaceHomeType() {
        return dlyPlaceHomeType;
    }

    public void setDlyPlaceHomeType(String dlyPlaceHomeType) {
        this.dlyPlaceHomeType = dlyPlaceHomeType;
    }

    public String getDlyPlacePublic() {
        return dlyPlacePublic;
    }

    public void setDlyPlacePublic(String dlyPlacePublic) {
        this.dlyPlacePublic = dlyPlacePublic;
    }

    public String getDlyPlacePrivate() {
        return dlyPlacePrivate;
    }

    public void setDlyPlacePrivate(String dlyPlacePrivate) {
        this.dlyPlacePrivate = dlyPlacePrivate;
    }

    public String getDlyType() {
        return dlyType;
    }

    public void setDlyType(String dlyType) {
        this.dlyType = dlyType;
    }

    public String getDlyComplication() {
        return dlyComplication;
    }

    public void setDlyComplication(String dlyComplication) {
        this.dlyComplication = dlyComplication;
    }

    public String getDischargeDate() {
        return dischargeDate;
    }

    public void setDischargeDate(String dischargeDate) {
        this.dischargeDate = dischargeDate;
    }

    public String getjSYPaidDate() {
        return jSYPaidDate;
    }

    public void setjSYPaidDate(String jSYPaidDate) {
        this.jSYPaidDate = jSYPaidDate;
    }

    public String getAbortion() {
        return abortion;
    }

    public void setAbortion(String abortion) {
        this.abortion = abortion;
    }

    public String getpNCHomeVisit() {
        return pNCHomeVisit;
    }

    public void setpNCHomeVisit(String pNCHomeVisit) {
        this.pNCHomeVisit = pNCHomeVisit;
    }

    public String getpNCComplication() {
        return pNCComplication;
    }

    public void setpNCComplication(String pNCComplication) {
        this.pNCComplication = pNCComplication;
    }

    public String getpPCMethod() {
        return pPCMethod;
    }

    public void setpPCMethod(String pPCMethod) {
        this.pPCMethod = pPCMethod;
    }

    public String getpNCCheckup() {
        return pNCCheckup;
    }

    public void setpNCCheckup(String pNCCheckup) {
        this.pNCCheckup = pNCCheckup;
    }

    public Integer getOutcomeNos() {
        return outcomeNos;
    }

    public void setOutcomeNos(Integer outcomeNos) {
        this.outcomeNos = outcomeNos;
    }

    public String getChild1Name() {
        return child1Name;
    }

    public void setChild1Name(String child1Name) {
        this.child1Name = child1Name;
    }

    public String getChild1Sex() {
        return child1Sex;
    }

    public void setChild1Sex(String child1Sex) {
        this.child1Sex = child1Sex;
    }

    public Double getChild1Wt() {
        return child1Wt;
    }

    public void setChild1Wt(Double child1Wt) {
        this.child1Wt = child1Wt;
    }

    public String getChild1Brestfeeding() {
        return child1Brestfeeding;
    }

    public void setChild1Brestfeeding(String child1Brestfeeding) {
        this.child1Brestfeeding = child1Brestfeeding;
    }

    public String getChild2Name() {
        return child2Name;
    }

    public void setChild2Name(String child2Name) {
        this.child2Name = child2Name;
    }

    public String getChild2Sex() {
        return child2Sex;
    }

    public void setChild2Sex(String child2Sex) {
        this.child2Sex = child2Sex;
    }

    public Double getChild2Wt() {
        return child2Wt;
    }

    public void setChild2Wt(Double child2Wt) {
        this.child2Wt = child2Wt;
    }

    public String getChild2Brestfeeding() {
        return child2Brestfeeding;
    }

    public void setChild2Brestfeeding(String child2Brestfeeding) {
        this.child2Brestfeeding = child2Brestfeeding;
    }

    public String getChild3Name() {
        return child3Name;
    }

    public void setChild3Name(String child3Name) {
        this.child3Name = child3Name;
    }

    public String getChild3Sex() {
        return child3Sex;
    }

    public void setChild3Sex(String child3Sex) {
        this.child3Sex = child3Sex;
    }

    public Double getChild3Wt() {
        return child3Wt;
    }

    public void setChild3Wt(Double child3Wt) {
        this.child3Wt = child3Wt;
    }

    public String getChild3Brestfeeding() {
        return child3Brestfeeding;
    }

    public void setChild3Brestfeeding(String child3Brestfeeding) {
        this.child3Brestfeeding = child3Brestfeeding;
    }

    public String getChild4Name() {
        return child4Name;
    }

    public void setChild4Name(String child4Name) {
        this.child4Name = child4Name;
    }

    public String getChild4Sex() {
        return child4Sex;
    }

    public void setChild4Sex(String child4Sex) {
        this.child4Sex = child4Sex;
    }

    public Double getChild4Wt() {
        return child4Wt;
    }

    public void setChild4Wt(Double child4Wt) {
        this.child4Wt = child4Wt;
    }

    public String getChild4Brestfeeding() {
        return child4Brestfeeding;
    }

    public void setChild4Brestfeeding(String child4Brestfeeding) {
        this.child4Brestfeeding = child4Brestfeeding;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getmTHRREGDATE() {
        return mTHRREGDATE;
    }

    public void setmTHRREGDATE(String mTHRREGDATE) {
        this.mTHRREGDATE = mTHRREGDATE;
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

    public Integer getaNMID() {
        return aNMID;
    }

    public void setaNMID(Integer aNMID) {
        this.aNMID = aNMID;
    }

    public Integer getaSHAID() {
        return aSHAID;
    }

    public void setaSHAID(Integer aSHAID) {
        this.aSHAID = aSHAID;
    }

    public Boolean getCallAns() {
        return callAns;
    }

    public void setCallAns(Boolean callAns) {
        this.callAns = callAns;
    }

    public Integer getNoCallReason() {
        return noCallReason;
    }

    public void setNoCallReason(Integer noCallReason) {
        this.noCallReason = noCallReason;
    }

    public Integer getNoPhoneReason() {
        return noPhoneReason;
    }

    public void setNoPhoneReason(Integer noPhoneReason) {
        this.noPhoneReason = noPhoneReason;
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

    public Integer getAadharNo() {
        return aadharNo;
    }

    public void setAadharNo(Integer aadharNo) {
        this.aadharNo = aadharNo;
    }

    public Integer getbPLAPL() {
        return bPLAPL;
    }

    public void setbPLAPL(Integer bPLAPL) {
        this.bPLAPL = bPLAPL;
    }

    public Integer geteID() {
        return eID;
    }

    public void seteID(Integer eID) {
        this.eID = eID;
    }

    public String geteIDTime() {
        return eIDTime;
    }

    public void seteIDTime(String eIDTime) {
        this.eIDTime = eIDTime;
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

    public Long getCaseNo() {
        return caseNo;
    }

    public void setCaseNo(Long caseNo) {
        this.caseNo = caseNo;
    }

    public String getMobileNo() {
        return mobileNo;
    }

    public void setMobileNo(String mobileNo) {
        this.mobileNo = mobileNo;
    }

    public String getAbortionType() {
        return abortionType;
    }

    public void setAbortionType(String abortionType) {
        this.abortionType = abortionType;
    }

    public String getDeliveryOutcomes() {
        return deliveryOutcomes;
    }

    public void setDeliveryOutcomes(String deliveryOutcomes) {
        this.deliveryOutcomes = deliveryOutcomes;
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

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
