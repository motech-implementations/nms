package org.motechproject.nms.flw.utils;

import org.joda.time.LocalDate;
import org.motechproject.nms.flw.domain.FlwJobStatus;
import org.motechproject.nms.flw.domain.FrontLineWorker;
import org.motechproject.nms.flw.domain.FrontLineWorkerStatus;
import org.motechproject.nms.region.domain.State;
import org.motechproject.nms.region.domain.District;
import org.motechproject.nms.region.domain.Taluka;
import org.motechproject.nms.region.domain.HealthBlock;
import org.motechproject.nms.region.domain.HealthFacility;
import org.motechproject.nms.region.domain.HealthSubFacility;
import org.motechproject.nms.region.domain.Village;
import org.motechproject.nms.region.exception.InvalidLocationException;

import java.util.Map;

/**
 * Helper class to set flw properties
 */
public final class FlwMapper {

    private FlwMapper() { }

    public static FrontLineWorker createFlw(Map<String, Object> record, Map<String, Object> location)
            throws InvalidLocationException {
        Long contactNumber = (Long) record.get(FlwConstants.CONTACT_NO);

        FrontLineWorker flw = new FrontLineWorker(contactNumber);
        flw.setStatus(FrontLineWorkerStatus.INACTIVE);

        return updateFlw(flw, record, location);
    }


    public static FrontLineWorker updateFlw(FrontLineWorker flw, Map<String, Object> record, Map<String, Object> location)
            throws InvalidLocationException {

        String mctsFlwId = (String) record.get(FlwConstants.ID);
        Long contactNumber = (Long) record.get(FlwConstants.CONTACT_NO);
        String name = (String) record.get(FlwConstants.NAME);
        String type = (String) record.get(FlwConstants.TYPE);
        String gfStatus = (String) record.get(FlwConstants.GF_STATUS);

        if (contactNumber != null) {
            flw.setContactNumber(contactNumber);
        }

        if (mctsFlwId != null) {
            flw.setMctsFlwId(mctsFlwId);
        }

        if (name != null) {
            flw.setName(name);
        }

        if(!gfStatus.isEmpty() && gfStatus!= null) {
            FlwJobStatus jobStatus = ("Active").equals(gfStatus) ? FlwJobStatus.ACTIVE : FlwJobStatus.INACTIVE;
            flw.setJobStatus(jobStatus);
        }

        setFrontLineWorkerLocation(flw, location);

        if (flw.getLanguage() == null) {
            flw.setLanguage(flw.getDistrict().getLanguage());
        }

        if (flw.getDesignation() == null) {
            flw.setDesignation(type);
        }

        if (record.get(FlwConstants.UPDATED_ON) != null) {
            flw.setUpdatedDateNic((LocalDate) record.get(FlwConstants.UPDATED_ON));
        }


        return flw;
    }

    public static FrontLineWorker setFrontLineWorkerLocation(FrontLineWorker flw, Map<String, Object> locations) throws InvalidLocationException {
        if (locations.get(FlwConstants.STATE_ID) == null && locations.get(FlwConstants.DISTRICT_ID) == null) {
            throw new InvalidLocationException("Missing mandatory state and district fields");
        }

        if (locations.get(FlwConstants.STATE_ID) == null) {
            throw new InvalidLocationException("Missing mandatory state field");
        }

        if (locations.get(FlwConstants.DISTRICT_ID) == null) {
            throw new InvalidLocationException("Missing mandatory district field");
        }

        flw.setState((State) locations.get(FlwConstants.STATE_ID));
        flw.setDistrict((District) locations.get(FlwConstants.DISTRICT_ID));
        flw.setTaluka((Taluka) locations.get(FlwConstants.TALUKA_ID));
        flw.setHealthBlock((HealthBlock) locations.get(FlwConstants.HEALTH_BLOCK_ID));
        flw.setHealthFacility((HealthFacility) locations.get(FlwConstants.PHC_ID));
        flw.setHealthSubFacility((HealthSubFacility) locations.get(FlwConstants.SUB_CENTRE_ID));
        flw.setVillage((Village) locations.get(FlwConstants.CENSUS_VILLAGE_ID + FlwConstants.NON_CENSUS_VILLAGE_ID));
        return flw;
    }
}
