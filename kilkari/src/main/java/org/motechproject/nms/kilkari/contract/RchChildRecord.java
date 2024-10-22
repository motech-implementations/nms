package org.motechproject.nms.kilkari.contract;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType
@XmlAccessorType(XmlAccessType.NONE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RchChildRecord {

    private Long stateId;
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
    private String name;
    private String mobileNo;
    private String rchAshaId;
    private String birthdate;
    private String registrationDate;
    private String registrationNo;
    private String motherRegistrationNo;
    private String entryType;
    private String mctsId;
    private String mctsMotherIdNo;
    private String execDate;

    public Long getStateId() {
        return stateId;
    }


    public String getDistrictName() {
        return districtName;
    }

    @XmlElement(name = "District_Name")
    @JsonProperty("District_Name")
    public void setDistrictName(String districtName) {
        this.districtName = districtName;
    }

    public String getTalukaId() {
        return talukaId;
    }

    @XmlElement(name = "Taluka_ID")
    @JsonProperty("Taluka_ID")
    public void setTalukaId(String talukaId) {
        this.talukaId = talukaId;
    }

    @XmlElement(name = "StateID")
    @JsonProperty("StateID")
    public void setStateId(Long stateId) {
        this.stateId = stateId;
    }

    public Long getDistrictId() {
        return districtId;
    }

    @XmlElement(name = "District_ID")
    @JsonProperty("District_ID")
    public void setDistrictId(Long districtId) {
        this.districtId = districtId;
    }

    public String getTalukaName() {
        return talukaName;
    }

    @XmlElement(name = "Taluka_Name")
    @JsonProperty("Taluka_Name")
    public void setTalukaName(String talukaName) {
        this.talukaName = talukaName;
    }

    public Long getHealthBlockId() {
        return healthBlockId;
    }

    public String getSubCentreName() {
        return subCentreName;
    }

    @XmlElement(name = "SubCentre_Name")
    @JsonProperty("SubCentre_Name")
    public void setSubCentreName(String subCentreName) {
        this.subCentreName = subCentreName;
    }

    public Long getVillageId() {
        return villageId;
    }

    public Long getSubCentreId() {
        return subCentreId;
    }

    @XmlElement(name = "SubCentre_ID")
    @JsonProperty("SubCentre_ID")
    public void setSubCentreId(Long subCentreId) {
        this.subCentreId = subCentreId;
    }

    @XmlElement(name = "Village_ID")
    @JsonProperty("Village_ID")
    public void setVillageId(Long villageId) {
        this.villageId = villageId;
    }

    @XmlElement(name = "HealthBlock_ID")
    @JsonProperty("HealthBlock_ID")
    public void setHealthBlockId(Long healthBlockId) {
        this.healthBlockId = healthBlockId;
    }

    public String getHealthBlockName() {
        return healthBlockName;
    }

    @XmlElement(name = "HealthBlock_Name")
    @JsonProperty("HealthBlock_Name")
    public void setHealthBlockName(String healthBlockName) {
        this.healthBlockName = healthBlockName;
    }

    public Long getPhcId() {
        return phcId;
    }

    @XmlElement(name = "PHC_ID")
    @JsonProperty("PHC_ID")
    public void setPhcId(Long phcId) {
        this.phcId = phcId;
    }

    public String getPhcName() {
        return phcName;
    }

    @XmlElement(name = "PHC_Name")
    @JsonProperty("PHC_Name")
    public void setPhcName(String phcName) {
        this.phcName = phcName;
    }

    public String getVillageName() {
        return villageName;
    }

    @XmlElement(name = "Village_Name")
    @JsonProperty("Village_Name")
    public void setVillageName(String villageName) {
        this.villageName = villageName;
    }

    public String getName() {
        return name;
    }

    @XmlElement(name = "Name")
    @JsonProperty("Name")
    public void setName(String name) {
        this.name = name;
    }

    public String getMobileNo() {
        return mobileNo;
    }

    @XmlElement(name = "Mobile_no")
    @JsonProperty("Mobile_no")
    public void setMobileNo(String mobileNo) {
        this.mobileNo = mobileNo;
    }

    public String getRchAshaId(){return rchAshaId; }

    @XmlElement(name = "Asha_ID")
    @JsonProperty("Asha_ID")
    public void setRchAshaId(String rchAshaId){ this.rchAshaId = rchAshaId; }

    public String getBirthdate() {
        return birthdate;
    }

    @XmlElement(name = "Birthdate")
    @JsonProperty("Birthdate")
    public void setBirthdate(String birthdate) {
        this.birthdate = birthdate;
    }

    public String getRegistrationDate() {
        return registrationDate;
    }

    @XmlElement(name = "Registration_Date")
    @JsonProperty("Registration_Date")
    public void setRegistrationDate(String registrationDate) {
        this.registrationDate = registrationDate;
    }

    public String getRegistrationNo() {
        return registrationNo;
    }

    @XmlElement(name = "Registration_no")
    @JsonProperty("Registration_no")
    public void setRegistrationNo(String registrationNo) {
        this.registrationNo = registrationNo;
    }

    public String getMotherRegistrationNo() {
        return motherRegistrationNo;
    }

    @XmlElement(name = "Mother_Registration_no")
    @JsonProperty("Mother_Registration_no")
    public void setMotherRegistrationNo(String motherRegistrationNo) {
        this.motherRegistrationNo = motherRegistrationNo;
    }

    public String getMctsId() {
        return mctsId;
    }

    @XmlElement(name = "MCTS_ID_No")
    @JsonProperty("MCTS_ID_No")
    public void setMctsId(String mctsId) {
        this.mctsId = mctsId;
    }

    public String getMctsMotherIdNo() {
        return mctsMotherIdNo;
    }

    @XmlElement(name = "MCTS_Mother_ID_No")
    @JsonProperty("MCTS_Mother_ID_No")
    public void setMctsMotherIdNo(String mctsMotherIdNo) {
        this.mctsMotherIdNo = mctsMotherIdNo;
    }

    public String getEntryType() {
        return entryType;
    }

    @XmlElement(name = "Entry_Type")
    @JsonProperty("Entry_Type")
    public void setEntryType(String entryType) {
        this.entryType = entryType;
    }

    public String getExecDate() {
        return execDate;
    }

    @XmlElement(name = "Exec_Date")
    @JsonProperty("Exec_Date")
    public void setExecDate(String execDate) {
        this.execDate = execDate;
    }
}
