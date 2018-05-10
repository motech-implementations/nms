package org.motechproject.nms.region.domain;


import java.util.HashMap;
import java.util.Map;

/**
 * Created by beehyv on 16/4/18.
 */
public class LocationFinder {

    private Map<String, State> stateHashMap = new HashMap<>();
    private Map<String, District> districtHashMap = new HashMap<>();
    private Map<String, Taluka> talukaHashMap = new HashMap<>();
    private Map<String, Village> villageHashMap = new HashMap<>();
    private Map<String, HealthBlock> healthBlockHashMap = new HashMap<>();
    private Map<String, HealthFacility> healthFacilityHashMap = new HashMap<>();
    private Map<String, HealthSubFacility> healthSubFacilityHashMap = new HashMap<>();

    public Map<String, State> getStateHashMap() {
        return stateHashMap;
    }

    public void setStateHashMap(Map<String, State> stateHashMap) {
        this.stateHashMap = stateHashMap;
    }

    public Map<String, District> getDistrictHashMap() {
        return districtHashMap;
    }

    public void setDistrictHashMap(Map<String, District> districtHashMap) {
        this.districtHashMap = districtHashMap;
    }

    public Map<String, Taluka> getTalukaHashMap() {
        return talukaHashMap;
    }

    public void setTalukaHashMap(Map<String, Taluka> talukaHashMap) {
        this.talukaHashMap = talukaHashMap;
    }

    public Map<String, Village> getVillageHashMap() {
        return villageHashMap;
    }

    public void setVillageHashMap(Map<String, Village> villageHashMap) {
        this.villageHashMap = villageHashMap;
    }

    public Map<String, HealthBlock> getHealthBlockHashMap() {
        return healthBlockHashMap;
    }

    public void setHealthBlockHashMap(Map<String, HealthBlock> healthBlockHashMap) {
        this.healthBlockHashMap = healthBlockHashMap;
    }

    public Map<String, HealthFacility> getHealthFacilityHashMap() {
        return healthFacilityHashMap;
    }

    public void setHealthFacilityHashMap(Map<String, HealthFacility> healthFacilityHashMap) {
        this.healthFacilityHashMap = healthFacilityHashMap;
    }

    public Map<String, HealthSubFacility> getHealthSubFacilityHashMap() {
        return healthSubFacilityHashMap;
    }

    public void setHealthSubFacilityHashMap(Map<String, HealthSubFacility> healthSubFacilityHashMap) {
        this.healthSubFacilityHashMap = healthSubFacilityHashMap;
    }
}
