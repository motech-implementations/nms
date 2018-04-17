package org.motechproject.nms.region.domain;


import java.util.HashMap;

/**
 * Created by beehyv on 16/4/18.
 */
public class LocationFinder {

    private HashMap<String, State> stateHashMap = new HashMap<>();
    private HashMap<String, District> districtHashMap = new HashMap<>();
    private HashMap<String, Taluka> talukaHashMap = new HashMap<>();
    private HashMap<String, Village> villageHashMap = new HashMap<>();
    private HashMap<String, HealthBlock> healthBlockHashMap = new HashMap<>();
    private HashMap<String, HealthFacility> healthFacilityHashMap = new HashMap<>();
    private HashMap<String, HealthSubFacility> healthSubFacilityHashMap = new HashMap<>();

    public HashMap<String, State> getStateHashMap() {
        return stateHashMap;
    }

    public void setStateHashMap(HashMap<String, State> stateHashMap) {
        this.stateHashMap = stateHashMap;
    }

    public HashMap<String, District> getDistrictHashMap() {
        return districtHashMap;
    }

    public void setDistrictHashMap(HashMap<String, District> districtHashMap) {
        this.districtHashMap = districtHashMap;
    }

    public HashMap<String, Taluka> getTalukaHashMap() {
        return talukaHashMap;
    }

    public void setTalukaHashMap(HashMap<String, Taluka> talukaHashMap) {
        this.talukaHashMap = talukaHashMap;
    }

    public HashMap<String, Village> getVillageHashMap() {
        return villageHashMap;
    }

    public void setVillageHashMap(HashMap<String, Village> villageHashMap) {
        this.villageHashMap = villageHashMap;
    }

    public HashMap<String, HealthBlock> getHealthBlockHashMap() {
        return healthBlockHashMap;
    }

    public void setHealthBlockHashMap(HashMap<String, HealthBlock> healthBlockHashMap) {
        this.healthBlockHashMap = healthBlockHashMap;
    }

    public HashMap<String, HealthFacility> getHealthFacilityHashMap() {
        return healthFacilityHashMap;
    }

    public void setHealthFacilityHashMap(HashMap<String, HealthFacility> healthFacilityHashMap) {
        this.healthFacilityHashMap = healthFacilityHashMap;
    }

    public HashMap<String, HealthSubFacility> getHealthSubFacilityHashMap() {
        return healthSubFacilityHashMap;
    }

    public void setHealthSubFacilityHashMap(HashMap<String, HealthSubFacility> healthSubFacilityHashMap) {
        this.healthSubFacilityHashMap = healthSubFacilityHashMap;
    }
}
