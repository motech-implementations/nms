package org.motechproject.nms.api.web.contract;

/**
 * Request body for creating or updating an flw
 * This is used primarily as an ops task to add/update flw properties from broken MCTS sync
 */
public class AddFlwRequest {
    private String name;
    private String mctsFlwId;
    private Long contactNumber;
    private Long stateId;
    private Long districtId;
    private String talukaId;
    private Long phcId;
    private Long subcentreId;
    private Long villageId;
    private Long healthblockId;
    private String type;
    private String gfStatus;

    public AddFlwRequest() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMctsFlwId() {
        return mctsFlwId;
    }

    public void setMctsFlwId(String mctsFlwId) {
        this.mctsFlwId = mctsFlwId;
    }

    public Long getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(Long contactNumber) {
        this.contactNumber = contactNumber;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getGfStatus() { return gfStatus; }

    public void setGfStatus(String gfStatus) { this.gfStatus = gfStatus; }
}
