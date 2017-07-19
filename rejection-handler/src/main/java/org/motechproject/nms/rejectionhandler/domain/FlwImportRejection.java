package org.motechproject.nms.rejectionhandler.domain;

import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;

import java.util.Date;

/**
 * Created by vishnu on 12/7/17.
 */
@Entity(tableName = "nms_flw_rejects")
public class FlwImportRejection {

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
    private Long flwId;

    @Field
    private String msisdn;

    @Field
    private String gfName;

    @Field
    private String gfStatus;

    @Field
    private String execDate;

    @Field
    private String regDate;

    @Field
    private String sex;

    @Field
    private String type;

    @Field
    private String smsReply;

    @Field
    private Integer aadharNo;

    @Field
    private String createdOn;

    @Field
    private String updatedOn;

    @Field
    private Integer bankId;

    @Field
    private String branchName;

    @Field
    private String ifscIdCode;

    @Field
    private String bankName;

    @Field
    private String accountNumber;

    @Field
    private Boolean isAadharLinked;

    @Field
    private String verifyDate;

    @Field
    private String verifierName;

    @Field
    private Integer verifierId;

    @Field
    private Boolean callAns;

    @Field
    private Boolean isPhoneNoCorrect;

    @Field
    private Integer noCallReason;

    @Field
    private Integer noPhoneReason;

    @Field
    private String verifierRemarks;

    @Field
    private String gfAddress;

    @Field
    private String husbandName;

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

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
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

    public Long getFlwId() {
        return flwId;
    }

    public void setFlwId(Long flwId) {
        this.flwId = flwId;
    }

    public String getMsisdn() {
        return msisdn;
    }

    public void setMsisdn(String msisdn) {
        this.msisdn = msisdn;
    }

    public String getGfName() {
        return gfName;
    }

    public void setGfName(String gfName) {
        this.gfName = gfName;
    }

    public String getGfStatus() {
        return gfStatus;
    }

    public void setGfStatus(String gfStatus) {
        this.gfStatus = gfStatus;
    }

    public String getExecDate() {
        return execDate;
    }

    public void setExecDate(String execDate) {
        this.execDate = execDate;
    }

    public String getRegDate() {
        return regDate;
    }

    public void setRegDate(String regDate) {
        this.regDate = regDate;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSmsReply() {
        return smsReply;
    }

    public void setSmsReply(String smsReply) {
        this.smsReply = smsReply;
    }

    public Integer getAadharNo() {
        return aadharNo;
    }

    public void setAadharNo(Integer aadharNo) {
        this.aadharNo = aadharNo;
    }

    public String getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(String createdOn) {
        this.createdOn = createdOn;
    }

    public String getUpdatedOn() {
        return updatedOn;
    }

    public void setUpdatedOn(String updatedOn) {
        this.updatedOn = updatedOn;
    }

    public Integer getBankId() {
        return bankId;
    }

    public void setBankId(Integer bankId) {
        this.bankId = bankId;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getIfscIdCode() {
        return ifscIdCode;
    }

    public void setIfscIdCode(String ifscIdCode) {
        this.ifscIdCode = ifscIdCode;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public Boolean getAadharLinked() {
        return isAadharLinked;
    }

    public void setAadharLinked(Boolean aadharLinked) {
        isAadharLinked = aadharLinked;
    }

    public String getVerifyDate() {
        return verifyDate;
    }

    public void setVerifyDate(String verifyDate) {
        this.verifyDate = verifyDate;
    }

    public String getVerifierName() {
        return verifierName;
    }

    public void setVerifierName(String verifierName) {
        this.verifierName = verifierName;
    }

    public Integer getVerifierId() {
        return verifierId;
    }

    public void setVerifierId(Integer verifierId) {
        this.verifierId = verifierId;
    }

    public Boolean getCallAns() {
        return callAns;
    }

    public void setCallAns(Boolean callAns) {
        this.callAns = callAns;
    }

    public Boolean getPhoneNoCorrect() {
        return isPhoneNoCorrect;
    }

    public void setPhoneNoCorrect(Boolean phoneNoCorrect) {
        isPhoneNoCorrect = phoneNoCorrect;
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

    public String getVerifierRemarks() {
        return verifierRemarks;
    }

    public void setVerifierRemarks(String verifierRemarks) {
        this.verifierRemarks = verifierRemarks;
    }

    public String getGfAddress() {
        return gfAddress;
    }

    public void setGfAddress(String gfAddress) {
        this.gfAddress = gfAddress;
    }

    public String getHusbandName() {
        return husbandName;
    }

    public void setHusbandName(String husbandName) {
        this.husbandName = husbandName;
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
}

