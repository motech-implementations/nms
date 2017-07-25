package org.motechproject.nms.rejectionhandler.domain;

import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Date;

/**
 * Created by beehyv on 13/7/17.
 */
@Entity(tableName = "nms_mother_rejects")
public class MotherImportRejection {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
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
    private String GPVillage;

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
    private String whomPhoneNumber;

    @Field
    private String birthDate;

    @Field
    private String JSYBeneficiary;

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
    private String deliveryLnkFacility;

    @Field
    private String facilityName;

    @Field
    private String lmpDate;

    @Field
    private String ANC1Date;

    @Field
    private String ANC2Date;

    @Field
    private String ANC3Date;

    @Field
    private String ANC4Date;

    @Field
    private String TT1Date;

    @Field
    private String TT2Date;

    @Field
    private String TTBoosterDate;

    @Field
    private String IFA100GivenDate;

    @Field
    private String anemia;

    @Field
    private String ANCComplication;

    @Field
    private String RTISTI;

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
    private String JSYPaidDate;

    @Field
    private String abortion;

    @Field
    private String PNCHomeVisit;

    @Field
    private String PNCComplication;

    @Field
    private String PPCMethod;

    @Field
    private String PNCCheckup;

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
    private String MTHRREGDATE;

    @Field
    private String lastUpdateDate;

    @Field
    private String remarks;

    @Field
    private Integer ANMID;

    @Field
    private Integer ASHAID;

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
    private Integer BPLAPL;

    @Field
    private Integer EID;

    @Field
    private String EIDTime;

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

    public String getJSYBeneficiary() {
        return JSYBeneficiary;
    }

    public void setJSYBeneficiary(String JSYBeneficiary) {
        this.JSYBeneficiary = JSYBeneficiary;
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

    public String getANC1Date() {
        return ANC1Date;
    }

    public void setANC1Date(String ANC1Date) {
        this.ANC1Date = ANC1Date;
    }

    public String getANC2Date() {
        return ANC2Date;
    }

    public void setANC2Date(String ANC2Date) {
        this.ANC2Date = ANC2Date;
    }

    public String getANC3Date() {
        return ANC3Date;
    }

    public void setANC3Date(String ANC3Date) {
        this.ANC3Date = ANC3Date;
    }

    public String getANC4Date() {
        return ANC4Date;
    }

    public void setANC4Date(String ANC4Date) {
        this.ANC4Date = ANC4Date;
    }

    public String getTT1Date() {
        return TT1Date;
    }

    public void setTT1Date(String TT1Date) {
        this.TT1Date = TT1Date;
    }

    public String getTT2Date() {
        return TT2Date;
    }

    public void setTT2Date(String TT2Date) {
        this.TT2Date = TT2Date;
    }

    public String getTTBoosterDate() {
        return TTBoosterDate;
    }

    public void setTTBoosterDate(String TTBoosterDate) {
        this.TTBoosterDate = TTBoosterDate;
    }

    public String getIFA100GivenDate() {
        return IFA100GivenDate;
    }

    public void setIFA100GivenDate(String IFA100GivenDate) {
        this.IFA100GivenDate = IFA100GivenDate;
    }

    public String getAnemia() {
        return anemia;
    }

    public void setAnemia(String anemia) {
        this.anemia = anemia;
    }

    public String getANCComplication() {
        return ANCComplication;
    }

    public void setANCComplication(String ANCComplication) {
        this.ANCComplication = ANCComplication;
    }

    public String getRTISTI() {
        return RTISTI;
    }

    public void setRTISTI(String RTISTI) {
        this.RTISTI = RTISTI;
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

    public String getJSYPaidDate() {
        return JSYPaidDate;
    }

    public void setJSYPaidDate(String JSYPaidDate) {
        this.JSYPaidDate = JSYPaidDate;
    }

    public String getAbortion() {
        return abortion;
    }

    public void setAbortion(String abortion) {
        this.abortion = abortion;
    }

    public String getPNCHomeVisit() {
        return PNCHomeVisit;
    }

    public void setPNCHomeVisit(String PNCHomeVisit) {
        this.PNCHomeVisit = PNCHomeVisit;
    }

    public String getPNCComplication() {
        return PNCComplication;
    }

    public void setPNCComplication(String PNCComplication) {
        this.PNCComplication = PNCComplication;
    }

    public String getPPCMethod() {
        return PPCMethod;
    }

    public void setPPCMethod(String PPCMethod) {
        this.PPCMethod = PPCMethod;
    }

    public String getPNCCheckup() {
        return PNCCheckup;
    }

    public void setPNCCheckup(String PNCCheckup) {
        this.PNCCheckup = PNCCheckup;
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

    public String getMTHRREGDATE() {
        return MTHRREGDATE;
    }

    public void setMTHRREGDATE(String MTHRREGDATE) {
        this.MTHRREGDATE = MTHRREGDATE;
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

    public Integer getASHAID() {
        return ASHAID;
    }

    public void setASHAID(Integer ASHAID) {
        this.ASHAID = ASHAID;
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

    public Integer getBPLAPL() {
        return BPLAPL;
    }

    public void setBPLAPL(Integer BPLAPL) {
        this.BPLAPL = BPLAPL;
    }

    public Integer getEID() {
        return EID;
    }

    public void setEID(Integer EID) {
        this.EID = EID;
    }

    public String getEIDTime() {
        return EIDTime;
    }

    public void setEIDTime(String EIDTime) {
        this.EIDTime = EIDTime;
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
