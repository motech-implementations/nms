package org.motechproject.nms.region.domain;

/**
 * Interface that marks a class as providing the full location hierachy.  Used along with the @ValidLocation
 * annotation
 */
public interface FullLocation {
    State getState();
    void setState(State state);

    District getDistrict();
    void setDistrict(District district);

    Taluka getTaluka();
    void setTaluka(Taluka taluka);

    Village getVillage();
    void setVillage(Village village);

    HealthBlock getHealthBlock();
    void setHealthBlock(HealthBlock healthBlock);

    HealthFacility getHealthFacility();
    void setHealthFacility(HealthFacility primaryHealthCenter);

    HealthSubFacility getHealthSubFacility();
    void setHealthSubFacility(HealthSubFacility healthSubFacility);
}
