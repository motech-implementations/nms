package org.motechproject.nms.kilkari.contract;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlElement;

@XmlType
@XmlAccessorType(XmlAccessType.NONE)
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
    private String lmpDate;
    private String birthDate;
    private String abortionType;
    private String deliveryOutcomes;
    private Integer entryType;
    private String execDate;

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

    @XmlElement(name = "District_ID")
    public void setDistrictId(Long districtId) {
        this.districtId = districtId;
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

    public String getMctsIdNo() {
        return mctsIdNo;
    }

    @XmlElement(name = "MCTS_ID_No")
    public void setMctsIdNo(String mctsIdNo) {
        this.mctsIdNo = mctsIdNo;
    }

    public String getRegistrationNo() {
        return registrationNo;
    }

    @XmlElement(name = "Registration_no")
    public void setRegistrationNo(String registrationNo) {
        this.registrationNo = registrationNo;
    }

    public Long getCaseNo() {
        return caseNo;
    }

    @XmlElement(name = "Case_no")
    public void setCaseNo(Long caseNo) {
        this.caseNo = caseNo;
    }

    public String getName() {
        return name;
    }

    @XmlElement(name = "Name")
    public void setName(String name) {
        this.name = name;
    }

    public String getMobileNo() {
        return mobileNo;
    }

    @XmlElement(name = "Mobile_no")
    public void setMobileNo(String mobileNo) {
        this.mobileNo = mobileNo;
    }

    public String getLmpDate() {
        return lmpDate;
    }

    @XmlElement(name = "LMP_Date")
    public void setLmpDate(String lmpDate) {
        this.lmpDate = lmpDate;
    }

    public String getBirthDate() {
        return birthDate;
    }

    @XmlElement(name = "Birthdate")
    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public String getAbortionType() {
        return abortionType;
    }

    @XmlElement(name = "Abortion_Type")
    public void setAbortionType(String abortionType) {
        this.abortionType = abortionType;
    }

    public String getDeliveryOutcomes() {
        return deliveryOutcomes;
    }

    @XmlElement(name = "Delivery_Outcomes")
    public void setDeliveryOutcomes(String deliveryOutcomes) {
        this.deliveryOutcomes = deliveryOutcomes;
    }

    public Integer getEntryType() {
        return entryType;
    }

    @XmlElement(name = "Entry_Type")
    public void setEntryType(Integer entryType) {
        this.entryType = entryType;
    }

    public String getExecDate() {
        return execDate;
    }

    @XmlElement(name = "Exec_Date")
    public void setExecDate(String execDate) {
        this.execDate = execDate;
    }
}
