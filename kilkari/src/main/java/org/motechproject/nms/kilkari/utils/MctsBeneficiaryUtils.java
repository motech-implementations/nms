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
import org.motechproject.nms.region.domain.LocationFinder;
import org.motechproject.nms.region.exception.InvalidLocationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ift.CellProcessor;

import javax.validation.ConstraintViolation;
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

    public static void setLocationFieldsCSV(LocationFinder locationFinder, Map<String, Object> record, MctsBeneficiary beneficiary) throws InvalidLocationException {

        String mapKey = record.get(KilkariConstants.STATE_ID).toString();
        if (isValidID(record, KilkariConstants.STATE_ID) && (locationFinder.getStateHashMap().get(mapKey) != null)) {
            beneficiary.setState(locationFinder.getStateHashMap().get(mapKey));
            String districtCode = record.get(KilkariConstants.DISTRICT_ID).toString();
            mapKey += "_";
            mapKey += districtCode;

            if (isValidID(record, KilkariConstants.DISTRICT_ID) && (locationFinder.getDistrictHashMap().get(mapKey) != null)) {
                beneficiary.setDistrict(locationFinder.getDistrictHashMap().get(mapKey));
                Long talukaCode = Long.parseLong(record.get(KilkariConstants.TALUKA_ID).toString());
                mapKey += "_";
                mapKey += talukaCode;
                beneficiary.setTaluka(locationFinder.getTalukaHashMap().get(mapKey));

                String villageSvid = record.get(KilkariConstants.NON_CENSUS_VILLAGE_ID) == null ? "0" : record.get(KilkariConstants.NON_CENSUS_VILLAGE_ID).toString();
                String villageCode = record.get(KilkariConstants.CENSUS_VILLAGE_ID) == null ? "0" : record.get(KilkariConstants.CENSUS_VILLAGE_ID).toString();
                String healthBlockCode = record.get(KilkariConstants.HEALTH_BLOCK_ID) == null ? "0" : record.get(KilkariConstants.HEALTH_BLOCK_ID).toString();
                String healthFacilityCode = record.get(KilkariConstants.PHC_ID) == null ? "0" : record.get(KilkariConstants.PHC_ID).toString();
                String healthSubFacilityCode = record.get(KilkariConstants.SUB_CENTRE_ID) == null ? "0" : record.get(KilkariConstants.SUB_CENTRE_ID).toString();

                beneficiary.setVillage(locationFinder.getVillageHashMap().get(mapKey + "_" + Long.parseLong(villageCode) + "_" + Long.parseLong(villageSvid)));
                mapKey += "_";
                mapKey += Long.parseLong(healthBlockCode);
                beneficiary.setHealthBlock(locationFinder.getHealthBlockHashMap().get(mapKey));
                mapKey += "_";
                mapKey += Long.parseLong(healthFacilityCode);
                beneficiary.setHealthFacility(locationFinder.getHealthFacilityHashMap().get(mapKey));
                mapKey += "_";
                mapKey += Long.parseLong(healthSubFacilityCode);
                beneficiary.setHealthSubFacility(locationFinder.getHealthSubFacilityHashMap().get(mapKey));
            } else {
                throw new InvalidLocationException(String.format(KilkariConstants.INVALID_LOCATION, KilkariConstants.DISTRICT_ID, record.get(KilkariConstants.DISTRICT_ID)));
            }
        } else {
            throw new InvalidLocationException(String.format(KilkariConstants.INVALID_LOCATION, KilkariConstants.STATE_ID, record.get(KilkariConstants.STATE_ID)));
        }
    }

    private static boolean isValidID(final Map<String, Object> map, final String key) {
        Object obj = map.get(key);
        if (obj == null || obj.toString().isEmpty() || "NULL".equalsIgnoreCase(obj.toString())) {
            return false;
        }

        if (obj.getClass().equals(Long.class)) {
            return (Long) obj > 0L;
        }

        return !"0".equals(obj);
    }
}
