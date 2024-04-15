package org.motechproject.nms.kilkari.contract;

import org.joda.time.LocalDate;
import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;
import org.motechproject.mds.annotations.InstanceLifecycleListeners;
import org.motechproject.nms.tracking.annotation.TrackClass;
import org.motechproject.nms.tracking.annotation.TrackFields;

@Entity(tableName = "nms_mother_table_data_xml")
@TrackClass
@TrackFields
@InstanceLifecycleListeners
public class RchMotherXMLData {
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
    private Long subCentreId;

    @Field
    private String subCentreName;

    @Field
    private Long villageId;

    @Field
    private String villageName;
    @Field
    private String mctsIdNo;

    @Field
    private String registrationNo;

    @Field
    private Long caseNo;

    @Field
    private String name;

    @Field
    private String mobileNo;
    @Field
    private String lmpDate;

    @Field
    private String birthDate;

    @Field
    private String registrationDate;

    @Field
    private String abortionType;

    @Field
    private String deliveryOutcomes;

    @Field
    private Integer entryType;

    @Field
    private String execDate;

    @Field
    private LocalDate xmlProcessingDate;

    public RchMotherXMLData(Long stateId, Long districtId, String districtName, String talukaId, String talukaName, Long healthBlockId, String healthBlockName, Long phcId, String phcName, Long subCentreId, String subCentreName, Long villageId, String villageName, String mctsIdNo, String registrationNo, Long caseNo, String name, String mobileNo, String registrationDate, String lmpDate, String birthDate, String abortionType, String deliveryOutcomes, Integer entryType, String execDate, LocalDate xmlProcessingDate) {
        this.stateId = stateId;
        this.districtId = districtId;
        this.districtName = districtName;
        this.talukaId = talukaId;
        this.talukaName = talukaName;
        this.healthBlockId = healthBlockId;
        this.healthBlockName = healthBlockName;
        this.phcId = phcId;
        this.phcName = phcName;
        this.subCentreId = subCentreId;
        this.subCentreName = subCentreName;
        this.villageId = villageId;
        this.villageName = villageName;
        this.mctsIdNo = mctsIdNo;
        this.registrationNo = registrationNo;
        this.caseNo = caseNo;
        this.name = name;
        this.mobileNo = mobileNo;
        this.registrationDate = registrationDate;
        this.lmpDate = lmpDate;
        this.birthDate = birthDate;
        this.abortionType = abortionType;
        this.deliveryOutcomes = deliveryOutcomes;
        this.entryType = entryType;
        this.execDate = execDate;
        this.xmlProcessingDate = xmlProcessingDate;
    }

    public LocalDate getXmlProcessingDate() {
        return xmlProcessingDate;
    }

    public void setXmlProcessingDate(LocalDate xmlProcessingDate) {
        this.xmlProcessingDate = xmlProcessingDate;
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

    public Long getSubCentreId() {
        return subCentreId;
    }

    public void setSubCentreId(Long subCentreId) {
        this.subCentreId = subCentreId;
    }

    public String getSubCentreName() {
        return subCentreName;
    }

    public void setSubCentreName(String subCentreName) {
        this.subCentreName = subCentreName;
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

    public String getMctsIdNo() {
        return mctsIdNo;
    }

    public void setMctsIdNo(String mctsIdNo) {
        this.mctsIdNo = mctsIdNo;
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

    public String getLmpDate() {
        return lmpDate;
    }

    public void setLmpDate(String lmpDate) {
        this.lmpDate = lmpDate;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
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

    public Integer getEntryType() {
        return entryType;
    }

    public void setEntryType(Integer entryType) {
        this.entryType = entryType;
    }

    public String getExecDate() {
        return execDate;
    }

    public void setExecDate(String execDate) {
        this.execDate = execDate;
    }

    public String getRegistrationDate() { return registrationDate; }

    public void setRegistrationDate(String registrationDate) { this.registrationDate = registrationDate; }

}
