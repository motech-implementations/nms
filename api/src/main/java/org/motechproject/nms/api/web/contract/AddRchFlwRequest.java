package org.motechproject.nms.api.web.contract;

/**
 * Created by beehyvsc on 17/7/17.
 */
public class AddRchFlwRequest {
    private String name;
    private String flwId;
    private Long msisdn;
    private Long stateId;
    private Long districtId;
    private String talukaId;
    private Long phcId;
    private Long subcentreId;
    private Long villageId;
    private Long healthblockId;
    private String gfType;
    private String gfStatus;

    public AddRchFlwRequest() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFlwId() {
        return flwId;
    }

    public void setFlwId(String flwId) {
        this.flwId = flwId;
    }

    public Long getMsisdn() {
        return msisdn;
    }

    public void setMsisdn(Long msisdn) {
        this.msisdn = msisdn;
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
}
