package org.motechproject.nms.kilkari.contract;

import org.joda.time.LocalDate;
import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;
import org.motechproject.mds.annotations.InstanceLifecycleListeners;
import org.motechproject.nms.tracking.annotation.TrackClass;
import org.motechproject.nms.tracking.annotation.TrackFields;

@Entity(tableName = "nms_child_table_data_xml")
@TrackClass
@TrackFields
@InstanceLifecycleListeners
public class RchChildXMLData {

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
    private String name;

    @Field
    private String mobileNo;

    @Field
    private String birthdate;

    @Field
    private String registrationNo;

    @Field
    private String registrationDate;

    @Field
    private String rchAshaId;

    @Field
    private String motherRegistrationNo;

    @Field
    private String entryType;

    @Field
    private String mctsId;

    @Field
    private String mctsMotherIdNo;

    @Field
    private String execDate;

    @Field
    private LocalDate xmlProcessingDate;

    public LocalDate getXmlProcessingDate() {
        return xmlProcessingDate;
    }

    public void setXmlProcessingDate(LocalDate xmlProcessingDate) {
        this.xmlProcessingDate = xmlProcessingDate;
    }

    public RchChildXMLData(Long stateId, Long districtId, String districtName, String talukaId, String talukaName, Long healthBlockId, String healthBlockName, Long phcId, String phcName, Long subCentreId, String subCentreName, Long villageId, String villageName, String name, String mobileNo, String birthdate, String registrationDate, String rchAshaId, String registrationNo, String motherRegistrationNo, String entryType, String mctsId, String mctsMotherIdNo, String execDate, LocalDate xmlProcessingDate) {
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
        this.name = name;
        this.mobileNo = mobileNo;
        this.birthdate = birthdate;
        this.registrationDate = registrationDate;
        this.rchAshaId = rchAshaId;
        this.registrationNo = registrationNo;
        this.motherRegistrationNo = motherRegistrationNo;
        this.entryType = entryType;
        this.mctsId = mctsId;
        this.mctsMotherIdNo = mctsMotherIdNo;
        this.execDate = execDate;
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

    public String getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(String birthdate) {
        this.birthdate = birthdate;
    }

    public String getRegistrationNo() {
        return registrationNo;
    }

    public void setRegistrationNo(String registrationNo) {
        this.registrationNo = registrationNo;
    }

    public String getMotherRegistrationNo() {
        return motherRegistrationNo;
    }

    public void setMotherRegistrationNo(String motherRegistrationNo) {
        this.motherRegistrationNo = motherRegistrationNo;
    }

    public String getEntryType() {
        return entryType;
    }

    public void setEntryType(String entryType) {
        this.entryType = entryType;
    }

    public String getMctsId() {
        return mctsId;
    }

    public void setMctsId(String mctsId) {
        this.mctsId = mctsId;
    }

    public String getMctsMotherIdNo() {
        return mctsMotherIdNo;
    }

    public void setMctsMotherIdNo(String mctsMotherIdNo) {
        this.mctsMotherIdNo = mctsMotherIdNo;
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
