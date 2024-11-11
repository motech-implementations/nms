package org.motechproject.nms.kilkari.contract;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.motechproject.nms.kilkari.utils.EntryTypeStrategy;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlElement;

@XmlType
@XmlAccessorType(XmlAccessType.NONE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RchMotherRecord {

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
    private String mctsIdNo;
    private String registrationNo;
    private Long caseNo;
    private String name;
    private String mobileNo;
    private String rchAshaId;
    private String lmpDate;
    private String registrationDate;
    private String birthDate;
    private String abortionType;
    private String deliveryOutcomes;
    private Integer entryType;
    private String execDate;

    public Long getStateId() {
        return stateId;
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

    public Long getHealthBlockId() {
        return healthBlockId;
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

    public String getTalukaName() {
        return talukaName;
    }

    @XmlElement(name = "Taluka_Name")
    @JsonProperty("Taluka_Name")
    public void setTalukaName(String talukaName) {
        this.talukaName = talukaName;
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

    public Long getSubCentreId() {
        return subCentreId;
    }

    @XmlElement(name = "SubCentre_ID")
    @JsonProperty("SubCentre_ID")
    public void setSubCentreId(Long subCentreId) {
        this.subCentreId = subCentreId;
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

    @XmlElement(name = "Village_ID")
    @JsonProperty("Village_ID")
    public void setVillageId(Long villageId) {
        this.villageId = villageId;
    }

    public String getVillageName() {
        return villageName;
    }

    @XmlElement(name = "Village_Name")
    @JsonProperty("Village_Name")
    public void setVillageName(String villageName) {
        this.villageName = villageName;
    }

    public String getMctsIdNo() {
        return mctsIdNo;
    }

    @XmlElement(name = "MCTS_ID_No")
    @JsonProperty("MCTS_ID_No")
    public void setMctsIdNo(String mctsIdNo) {
        this.mctsIdNo = mctsIdNo;
    }

    public String getRegistrationNo() {
        return registrationNo;
    }

    @XmlElement(name = "Registration_no")
    @JsonProperty("Registration_no")
    public void setRegistrationNo(String registrationNo) {
        this.registrationNo = registrationNo;
    }

    public Long getCaseNo() {
        return caseNo;
    }

    @XmlElement(name = "Case_no")
    @JsonProperty("Case_no")
    public void setCaseNo(Long caseNo) {
        this.caseNo = caseNo;
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

    public String getLmpDate() {
        return lmpDate;
    }

    @XmlElement(name = "LMP_Date")
    @JsonProperty("LMP_Date")
    public void setLmpDate(String lmpDate) {
        this.lmpDate = lmpDate;
    }

    public String getRegistrationDate(){ return registrationDate; }

    @XmlElement(name = "Mother_Registration_Date")
    @JsonProperty("Mother_Registration_Date")
    public void setRegistrationDate(String registrationDate) { this.registrationDate = registrationDate;}

    public String getBirthDate() {
        return birthDate;
    }

    @XmlElement(name = "Birthdate")
    @JsonProperty("Birthdate")
    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public String getAbortionType() {
        return abortionType;
    }

    @XmlElement(name = "Abortion_Type")
    @JsonProperty("Abortion_Type")
    public void setAbortionType(String abortionType) {
        this.abortionType = abortionType;
    }

    public String getDeliveryOutcomes() {
        return deliveryOutcomes;
    }

    @XmlElement(name = "Delivery_Outcomes")
    @JsonProperty("Delivery_Outcomes")
    public void setDeliveryOutcomes(String deliveryOutcomes) {
        this.deliveryOutcomes = deliveryOutcomes;
    }

    public Integer getEntryType() {
        return entryType;
    }

    @XmlElement(name = "Entry_Type")
    @JsonProperty("Entry_Type")
    public void setEntryType(Object entryType) {
        EntryTypeStrategy strategy = new EntryTypeStrategy();
        this.entryType = strategy.determineEntryType(entryType);
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
