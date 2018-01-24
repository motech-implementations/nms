package org.motechproject.nms.api.web.contract;

/**
 * Created by beehyvsc on 17/7/17.
 */
public class UpdateFlwLocationRequest {
    private String mctsFlwId;
    private Long stateId;
    private Long districtId;
    private String talukaId;
    private String talukaName;
    private Long phcId;
    private String phcName;
    private Long subcentreId;
    private String subcentreName;
    private Long villageId;
    private String villageName;
    private Long healthblockId;
    private String healthblockName;

    public UpdateFlwLocationRequest() {

    }

    public String getHealthblockName() {
        return healthblockName;
    }

    public void setHealthBlockName(String healthblockName) {
        this.healthblockName = healthblockName;
    }

    public String getTalukaName() {
        return talukaName;
    }

    public void setTalukaName(String talukaName) {
        this.talukaName = talukaName;
    }

    public String getPhcName() {
        return phcName;
    }

    public void setPhcName(String phcName) {
        this.phcName = phcName;
    }

    public String getSubcentreName() {
        return subcentreName;
    }

    public void setSubcentreName(String subcentreName) {
        this.subcentreName = subcentreName;
    }

    public String getVillageName() {
        return villageName;
    }

    public void setVillageName(String villageName) {
        this.villageName = villageName;
    }

    public String getMctsFlwId() {
        return mctsFlwId;
    }

    public void setMctsFlwId(String mctsFlwId) {
        this.mctsFlwId = mctsFlwId;
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

    public String getTalukaId() {
        return talukaId;
    }

    public void setTalukaId(String talukaId) {
        this.talukaId = talukaId;
    }

    public Long getPhcId() {
        return phcId;
    }

    public void setPhcId(Long phcId) {
        this.phcId = phcId;
    }

    public Long getSubcentreId() {
        return subcentreId;
    }

    public void setSubcentreId(Long subcentreId) {
        this.subcentreId = subcentreId;
    }

    public Long getVillageId() {
        return villageId;
    }

    public void setVillageId(Long villageId) {
        this.villageId = villageId;
    }

    public Long getHealthblockId() {
        return healthblockId;
    }

    public void setHealthblockId(Long healthblockId) {
        this.healthblockId = healthblockId;
    }
}
