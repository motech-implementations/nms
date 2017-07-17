package org.motechproject.nms.rejectionhandler.domain;

import org.joda.time.LocalDate;
import org.motechproject.commons.date.util.DateUtil;
import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;
import org.motechproject.nms.rch.domain.RchUserType;

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
    private Long talukaId;

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
    private Long msisdn;

    @Field
    private String gfName;

    @Field
    private String gfType;

    @Field
    private String gfStatus;

    @Field
    private Date execDate;

    @Field
    private Date regDate;

    @Field
    private String sex;

    @Field
    private String type;

    @Field
    private String smsReply;

    @Field
    private String aadharNo;

    @Field
    private Date createdOn;

    @Field
    private Date updatedOn;

    @Field
    private Long bankId;

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
    private Date verifyDate;

    @Field
    private String verifierName;

    @Field
    private String verifierId;

    @Field
    private Boolean callAns;

    @Field
    private Boolean isPhoneNoCorrect;

    @Field
    private String noCallReason;

    @Field
    private String noPhoneReason;

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

    public FlwImportRejection(Long stateId, Long districtId, String districtName, Long talukaId, String talukaName, Long healthBlockId,
                              String healthBlockName, Long phcId, String phcName, Long subcentreId, String subcentreName, Long villageId,
                              String villageName, Long flwId, Long msisdn, String gfName, String gfType, String gfStatus, Date execDate,
                              Date regDate, String sex, String type, String smsReply, String aadharNo, Date createdOn, Date updatedOn,
                              Long bankId, String branchName, String ifscIdCode, String bankName, String accountNumber, Boolean isAadharLinked,
                              Date verifyDate, String verifierName, String verifierId, Boolean callAns, Boolean isPhoneNoCorrect, String noCallReason,
                              String noPhoneReason, String verifierRemarks, String gfAddress, String husbandName, Boolean accepted, String rejectionReason,
                              String source, Date creationDate, Date modificationDate, String action) {
        this.stateId = stateId;
        this.districtId = districtId;
        this.districtName = districtName;
        this.talukaId = talukaId;
        this.talukaName = talukaName;
        this.healthBlockId = healthBlockId;
        this.healthBlockName = healthBlockName;
        this.phcId = phcId;
        this.phcName = phcName;
        this.subcentreId = subcentreId;
        this.subcentreName = subcentreName;
        this.villageId = villageId;
        this.villageName = villageName;
        this.flwId = flwId;
        this.msisdn = msisdn;
        this.gfName = gfName;
        this.gfType = gfType;
        this.gfStatus = gfStatus;
        this.execDate = execDate;
        this.regDate = regDate;
        this.sex = sex;
        this.type = type;
        this.smsReply = smsReply;
        this.aadharNo = aadharNo;
        this.createdOn = createdOn;
        this.updatedOn = updatedOn;
        this.bankId = bankId;
        this.branchName = branchName;
        this.ifscIdCode = ifscIdCode;
        this.bankName = bankName;
        this.accountNumber = accountNumber;
        this.isAadharLinked = isAadharLinked;
        this.verifyDate = verifyDate;
        this.verifierName = verifierName;
        this.verifierId = verifierId;
        this.callAns = callAns;
        this.isPhoneNoCorrect = isPhoneNoCorrect;
        this.noCallReason = noCallReason;
        this.noPhoneReason = noPhoneReason;
        this.verifierRemarks = verifierRemarks;
        this.gfAddress = gfAddress;
        this.husbandName = husbandName;
        this.accepted = accepted;
        this.rejectionReason = rejectionReason;
        this.source = source;
        this.creationDate = creationDate;
        this.modificationDate = modificationDate;
        this.action = action;
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

    public Long getTalukaId() {
        return talukaId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public void setTalukaId(Long talukaId) {
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

    public Long getMsisdn() {
        return msisdn;
    }

    public void setMsisdn(Long msisdn) {
        this.msisdn = msisdn;
    }

    public String getGfName() {
        return gfName;
    }

    public void setGfName(String gfName) {
        this.gfName = gfName;
    }

    public String getGfType() {
        return gfType;
    }

    public void setGfType(String gfType) {
        this.gfType = gfType;
    }

    public String getGfStatus() {
        return gfStatus;
    }

    public void setGfStatus(String gfStatus) {
        this.gfStatus = gfStatus;
    }

    public Date getExecDate() {
        return execDate;
    }

    public void setExecDate(Date execDate) {
        this.execDate = execDate;
    }

    public Date getRegDate() {
        return regDate;
    }

    public void setRegDate(Date regDate) {
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

    public String getAadharNo() {
        return aadharNo;
    }

    public void setAadharNo(String aadharNo) {
        this.aadharNo = aadharNo;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    public Date getUpdatedOn() {
        return updatedOn;
    }

    public void setUpdatedOn(Date updatedOn) {
        this.updatedOn = updatedOn;
    }

    public Long getBankId() {
        return bankId;
    }

    public void setBankId(Long bankId) {
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

    public Date getVerifyDate() {
        return verifyDate;
    }

    public void setVerifyDate(Date verifyDate) {
        this.verifyDate = verifyDate;
    }

    public String getVerifierName() {
        return verifierName;
    }

    public void setVerifierName(String verifierName) {
        this.verifierName = verifierName;
    }

    public String getVerifierId() {
        return verifierId;
    }

    public void setVerifierId(String verifierId) {
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

    public String getNoCallReason() {
        return noCallReason;
    }

    public void setNoCallReason(String noCallReason) {
        this.noCallReason = noCallReason;
    }

    public String getNoPhoneReason() {
        return noPhoneReason;
    }

    public void setNoPhoneReason(String noPhoneReason) {
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

