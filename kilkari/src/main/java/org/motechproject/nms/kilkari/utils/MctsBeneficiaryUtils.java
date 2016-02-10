package org.motechproject.nms.kilkari.utils;


import org.motechproject.nms.csv.exception.CsvImportDataException;
import org.motechproject.nms.csv.utils.ConstraintViolationUtils;
import org.motechproject.nms.csv.utils.GetLong;
import org.motechproject.nms.csv.utils.GetString;
import org.motechproject.nms.kilkari.domain.MctsBeneficiary;
import org.motechproject.nms.kilkari.utils.KilkariConstants;
import org.motechproject.nms.region.domain.District;
import org.motechproject.nms.region.domain.HealthBlock;
import org.motechproject.nms.region.domain.HealthFacility;
import org.motechproject.nms.region.domain.HealthSubFacility;
import org.motechproject.nms.region.domain.State;
import org.motechproject.nms.region.domain.Taluka;
import org.motechproject.nms.region.domain.Village;
import org.motechproject.nms.region.exception.InvalidLocationException;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ift.CellProcessor;

import javax.validation.ConstraintViolation;
import java.util.Map;
import java.util.Set;

public final class MctsBeneficiaryUtils {

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

        if (locations.get(KilkariConstants.MAPPER_STATE) == null && locations.get(KilkariConstants.MAPPER_DISTRICT) == null) {
            throw new InvalidLocationException("Missing mandatory state and district fields");
        }

        if (locations.get(KilkariConstants.MAPPER_STATE) == null) {
            throw new InvalidLocationException("Missing mandatory state field");
        }

        if (locations.get(KilkariConstants.MAPPER_DISTRICT) == null) {
            throw new InvalidLocationException("Missing mandatory district field");
        }

        beneficiary.setState((State) locations.get(KilkariConstants.MAPPER_STATE));
        beneficiary.setDistrict((District) locations.get(KilkariConstants.MAPPER_DISTRICT));
        beneficiary.setTaluka((Taluka) locations.get(KilkariConstants.MAPPER_TALUKA));
        beneficiary.setHealthBlock((HealthBlock) locations.get(KilkariConstants.MAPPER_HEALTH_BLOCK));
        beneficiary.setHealthFacility((HealthFacility) locations.get(KilkariConstants.MAPPER_PHC));
        beneficiary.setHealthSubFacility((HealthSubFacility) locations.get(KilkariConstants.MAPPER_SUBCENTRE));
        beneficiary.setVillage((Village) locations.get(KilkariConstants.MAPPER_CENSUS_VILLAGE + KilkariConstants.MAPPER_NON_CENSUS_VILLAGE));
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

}
