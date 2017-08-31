package org.motechproject.nms.flw.utils;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.motechproject.nms.flw.domain.FlwJobStatus;
import org.motechproject.nms.flw.domain.FrontLineWorker;
import org.motechproject.nms.flw.domain.FrontLineWorkerStatus;
import org.motechproject.nms.kilkari.domain.SubscriptionOrigin;
import org.motechproject.nms.kilkari.utils.FlwConstants;
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

    private static final String ACTIVE = "Active";

    private FlwMapper() { }

    public static FrontLineWorker createFlw(Map<String, Object> record, Map<String, Object> location)
            throws InvalidLocationException {
        Long contactNumber = (Long) record.get(FlwConstants.CONTACT_NO);
        String gfStatus = (String) record.get(FlwConstants.GF_STATUS);
        if (gfStatus != null && !gfStatus.isEmpty() && ACTIVE.equals(gfStatus)) {
            FrontLineWorker flw = new FrontLineWorker(contactNumber);
            flw.setStatus(FrontLineWorkerStatus.INACTIVE);

            return updateFlw(flw, record, location, SubscriptionOrigin.MCTS_IMPORT);
        } else {
            return null;
        }
    }

    // NO CHECKSTYLE Cyclomatic Complexity
    public static FrontLineWorker createRchFlw(Map<String, Object> record, Map<String, Object> location)
            throws InvalidLocationException {
        Long contactNumber = (Long) record.get(FlwConstants.MOBILE_NO);
        String gfStatus = (String) record.get(FlwConstants.GF_STATUS);
        if (gfStatus != null && !gfStatus.isEmpty() && ACTIVE.equals(gfStatus)) {
            FrontLineWorker flw = new FrontLineWorker(contactNumber);
            flw.setStatus(FrontLineWorkerStatus.INACTIVE);

            return updateFlw(flw, record, location, SubscriptionOrigin.RCH_IMPORT);
        } else {
            return null;
        }
    }

    // CHECKSTYLE:OFF
    public static FrontLineWorker updateFlw(FrontLineWorker flw, Map<String, Object> record, Map<String, Object> location, SubscriptionOrigin importOrigin)
            throws InvalidLocationException {

        String flwId;
        Long contactNumber;
        String name;
        String type;
        if (importOrigin.equals(SubscriptionOrigin.MCTS_IMPORT)) {
            flwId = (String) record.get(FlwConstants.ID);
            contactNumber = (Long) record.get(FlwConstants.CONTACT_NO);
            name = (String) record.get(FlwConstants.NAME);
            type = (String) record.get(FlwConstants.TYPE);
        } else {
            flwId = (String) record.get(FlwConstants.GF_ID);
            contactNumber = (Long) record.get(FlwConstants.MOBILE_NO);
            name = (String) record.get(FlwConstants.GF_NAME);
            type = (String) record.get(FlwConstants.GF_TYPE);
        }

        String gfStatus = (String) record.get(FlwConstants.GF_STATUS);

        if (contactNumber != null) {
            flw.setContactNumber(contactNumber);
        }

        if (flwId != null) {
            flw.setMctsFlwId(flwId);
        }

        if (name != null) {
            flw.setName(name);
        }

        if (gfStatus != null && !gfStatus.isEmpty()) {
            FlwJobStatus jobStatus = ACTIVE.equals(gfStatus) ? FlwJobStatus.ACTIVE : FlwJobStatus.INACTIVE;
            flw.setJobStatus(jobStatus);
        }

        setFrontLineWorkerLocation(flw, location);

        if (flw.getLanguage() == null) {
            flw.setLanguage(flw.getDistrict().getLanguage());
        }

        if (flw.getDesignation() == null) {
            flw.setDesignation(type);
        }

        LocalDate date;
        String datePattern = "\\d{4}-\\d{2}-\\d{2}";
        DateTimeFormatter dtf1 = DateTimeFormat.forPattern("yyyy-MM-dd");
        DateTimeFormatter dtf2 = DateTimeFormat.forPattern("dd-MM-yyyy");
        if (importOrigin.equals(SubscriptionOrigin.MCTS_IMPORT)) {
            date = record.get(FlwConstants.UPDATED_ON) == null || record.get(FlwConstants.UPDATED_ON).toString().trim().isEmpty() ? null :
                    (record.get(FlwConstants.UPDATED_ON).toString().matches(datePattern) ?
                            LocalDate.parse(record.get(FlwConstants.UPDATED_ON).toString(), dtf1) :
                            LocalDate.parse(record.get(FlwConstants.UPDATED_ON).toString(), dtf2));
        } else {
            date = record.get(FlwConstants.EXEC_DATE) == null || record.get(FlwConstants.EXEC_DATE).toString().trim().isEmpty() ? null :
                    (record.get(FlwConstants.EXEC_DATE).toString().matches(datePattern) ?
                            LocalDate.parse(record.get(FlwConstants.EXEC_DATE).toString(), dtf1) :
                            LocalDate.parse(record.get(FlwConstants.EXEC_DATE).toString(), dtf2));
        }
        if (date != null) {
            flw.setUpdatedDateNic(date);
        }


        return flw;
    }

    //CHECKSTYLE:ON
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
