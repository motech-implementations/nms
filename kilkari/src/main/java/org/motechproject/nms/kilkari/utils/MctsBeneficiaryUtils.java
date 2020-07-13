package org.motechproject.nms.kilkari.utils;


import org.motechproject.nms.csv.exception.CsvImportDataException;
import org.motechproject.nms.csv.utils.ConstraintViolationUtils;
import org.motechproject.nms.csv.utils.GetLong;
import org.motechproject.nms.csv.utils.GetString;
import org.motechproject.nms.kilkari.domain.MctsBeneficiary;
import org.motechproject.nms.region.domain.District;
import org.motechproject.nms.region.domain.HealthBlock;
import org.motechproject.nms.region.domain.HealthFacility;
import org.motechproject.nms.region.domain.HealthSubFacility;
import org.motechproject.nms.region.domain.State;
import org.motechproject.nms.region.domain.Taluka;
import org.motechproject.nms.region.domain.Village;
import org.motechproject.nms.region.exception.InvalidLocationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ift.CellProcessor;

import javax.validation.ConstraintViolation;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

public final class MctsBeneficiaryUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(MctsBeneficiaryUtils.class);

    private MctsBeneficiaryUtils() {
    }

    public static void getBeneficiaryLocationMapping(Map<String, CellProcessor> mapping) {
        mapping.put(KilkariConstants.STATE_ID, new Optional(new GetLong()));

        mapping.put(KilkariConstants.DISTRICT_ID, new Optional(new GetLong()));
        mapping.put(KilkariConstants.DISTRICT_NAME, new Optional(new GetString()));

        mapping.put(KilkariConstants.TALUKA_ID, new Optional(new GetString()));
        mapping.put(KilkariConstants.TALUKA_NAME, new Optional(new GetString()));

        mapping.put(KilkariConstants.CENSUS_VILLAGE_ID, new Optional(new GetLong()));
        mapping.put(KilkariConstants.NON_CENSUS_VILLAGE_ID, new Optional(new GetLong()));
        mapping.put(KilkariConstants.VILLAGE_NAME, new Optional(new GetString()));

        mapping.put(KilkariConstants.PHC_ID, new Optional(new GetLong()));
        mapping.put(KilkariConstants.PHC_NAME, new Optional(new GetString()));

        mapping.put(KilkariConstants.HEALTH_BLOCK_ID, new Optional(new GetLong()));
        mapping.put(KilkariConstants.HEALTH_BLOCK_NAME, new Optional(new GetString()));

        mapping.put(KilkariConstants.SUB_CENTRE_ID, new Optional(new GetLong()));
        mapping.put(KilkariConstants.SUB_CENTRE_NAME, new Optional(new GetString()));
    }

    public static void setLocationFields(Map<String, Object> locations, MctsBeneficiary beneficiary) throws InvalidLocationException {

        LOGGER.info("locations {}", locations);
        if (locations.get(KilkariConstants.STATE_ID) == null && locations.get(KilkariConstants.DISTRICT_ID) == null) {
            throw new InvalidLocationException("Missing mandatory state and district fields");
        }

        if (locations.get(KilkariConstants.STATE_ID) == null) {
            throw new InvalidLocationException("Missing mandatory state field");
        }

        if (locations.get(KilkariConstants.DISTRICT_ID) == null) {
            throw new InvalidLocationException("Missing mandatory district field");
        }
        beneficiary.setState((State) locations.get(KilkariConstants.STATE_ID));
        beneficiary.setDistrict((District) locations.get(KilkariConstants.DISTRICT_ID));
        beneficiary.setTaluka((Taluka) locations.get(KilkariConstants.TALUKA_ID));
        beneficiary.setHealthBlock((HealthBlock) locations.get(KilkariConstants.HEALTH_BLOCK_ID));
        beneficiary.setHealthFacility((HealthFacility) locations.get(KilkariConstants.PHC_ID));
        beneficiary.setHealthSubFacility((HealthSubFacility) locations.get(KilkariConstants.SUB_CENTRE_ID));
        beneficiary.setVillage((Village) locations.get(KilkariConstants.CENSUS_VILLAGE_ID + KilkariConstants.NON_CENSUS_VILLAGE_ID));
    }

    public static String createErrorMessage(String message, int rowNumber) {
        return String.format("CSV instance error [row: %d]: %s", rowNumber, message);
    }

    public static String createErrorMessage(Set<ConstraintViolation<?>> violations, int rowNumber) {
        return String.format("CSV instance error [row: %d]: validation failed for instance of type %s, violations: %s",
                rowNumber, MctsBeneficiary.class.getName(), ConstraintViolationUtils.toString(violations));
    }

    public static void verify(boolean condition, String message, String... args) {
        if (!condition) {
            throw new CsvImportDataException(String.format(message, args));
        }
    }

    public static void idCleanup(ArrayList<String > listOfIds, Map<String, Object> record){
        for(String id :listOfIds){
            if(record.get(id)!=null){
                String idValue=(String)record.get(id);
                idValue=idValue.replaceAll(KilkariConstants.SPECIAL_CHAR_STRING,"");
                record.replace(id,idValue);
            }
        }
    }
}
