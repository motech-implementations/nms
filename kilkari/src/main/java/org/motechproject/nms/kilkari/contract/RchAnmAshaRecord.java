package org.motechproject.nms.kilkari.contract;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.motechproject.nms.kilkari.utils.FlwConstants;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.HashMap;
import java.util.Map;

@XmlType
@XmlAccessorType(XmlAccessType.NONE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RchAnmAshaRecord {

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
    private Long gfId;
    private String mobileNo;
    private String gfName;
    private String gfType;
    private String execDate;
    private String gfStatus;
    private String updatedOn;

    @XmlElement(name = "Taluka_Name")
    @JsonProperty("Taluka_Name")
    public void setTalukaName(String talukaName) {
        this.talukaName = talukaName;
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

    public String getVillageName() {
        return villageName;
    }

    @XmlElement(name = "Village_Name")
    @JsonProperty("Village_Name")
    public void setVillageName(String villageName) {
        this.villageName = villageName;
    }

    public Long getGfId() {
        return gfId;
    }

    @XmlElement(name = "GF_ID")
    @JsonProperty("GF_ID")
    public void setGfId(Long gfId) {
        this.gfId = gfId;
    }

    public String getMobileNo() {
        return mobileNo;
    }

    @XmlElement(name = "HealthBlock_Name")
    @JsonProperty("HealthBlock_Name")
    public void setHealthBlockName(String healthBlockName) {
        this.healthBlockName = healthBlockName;
    }

    public Long getPhcId() {
        return phcId;
    }

    @XmlElement(name = "Mobile_no")
    @JsonProperty("Mobile_no")
    public void setMobileNo(String mobileNo) {
        this.mobileNo = mobileNo;
    }

    public String getGfName() {
        return gfName;
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

    @XmlElement(name = "Village_ID")
    @JsonProperty("Village_ID")
    public void setVillageId(Long villageId) {
        this.villageId = villageId;
    }

    @XmlElement(name = "GF_Name")
    @JsonProperty("GF_Name")
    public void setGfName(String gfName) {
        this.gfName = gfName;
    }

    public String getGfType() {
        return gfType;
    }

    @XmlElement(name = "GF_type")
    @JsonProperty("GF_type")
    public void setGfType(String gfType) {
        this.gfType = gfType;
    }

    public String getExecDate() {
        return execDate;
    }

    @XmlElement(name = "Exec_Date")
    @JsonProperty("Exec_Date")
    public void setExecDate(String execDate) {
        this.execDate = execDate;
    }

    public String getGfStatus() {
        return gfStatus;
    }

    @XmlElement(name = "GF_Status")
    @JsonProperty("GF_Status")
    public void setGfStatus(String gfStatus) {
        this.gfStatus = gfStatus;
    }

    public String getUpdatedOn() {
        return updatedOn;
    }

    @XmlElement(name = "Updated_On")
    @JsonProperty("Updated_On")
    public void setUpdatedOn(String updatedOn) {
        this.updatedOn = updatedOn;
    }

    public Map<String, Object> toFlwRecordMap() {
        Map<String, Object> map = new HashMap<>();
        map.put(FlwConstants.STATE_ID, getStateId());
        map.put(FlwConstants.DISTRICT_ID, getDistrictId());
        map.put(FlwConstants.DISTRICT_NAME, getDistrictName());
        map.put(FlwConstants.TALUKA_ID, getTalukaId());
        map.put(FlwConstants.TALUKA_NAME, getTalukaName());
        map.put(FlwConstants.HEALTH_BLOCK_ID, getHealthBlockId());
        map.put(FlwConstants.HEALTH_BLOCK_NAME, getHealthBlockName());
        map.put(FlwConstants.PHC_ID, getPhcId());
        map.put(FlwConstants.PHC_NAME, getPhcName());
        map.put(FlwConstants.SUB_CENTRE_ID, getSubCentreId());
        map.put(FlwConstants.SUB_CENTRE_NAME, getSubCentreName());
        map.put(FlwConstants.CENSUS_VILLAGE_ID, getVillageId());
        map.put(FlwConstants.VILLAGE_NAME, getVillageName());
        map.put(FlwConstants.GF_ID, getGfId() == null ? null : getGfId().toString());
        map.put(FlwConstants.MOBILE_NO, getMobileNo() == null ? null : Long.parseLong(getMobileNo()));
        map.put(FlwConstants.GF_NAME, getGfName());
        map.put(FlwConstants.GF_TYPE, getGfType());
        map.put(FlwConstants.EXEC_DATE, "".equals(getExecDate()) ? null : LocalDate.parse(getExecDate(), DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")));
        map.put(FlwConstants.GF_STATUS, getGfStatus());
        map.put(FlwConstants.UPDATED_ON, "".equals(getUpdatedOn()) ? null : getUpdatedOn());
        return map;
    }
}
