package org.motechproject.nms.kilkari.service.impl;


import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.joda.time.format.DateTimeParser;
import org.motechproject.nms.csv.exception.CsvImportDataException;
import org.motechproject.nms.csv.utils.ConstraintViolationUtils;
import org.motechproject.nms.csv.utils.GetInstanceByString;
import org.motechproject.nms.kilkari.domain.MctsBeneficiary;
import org.motechproject.nms.region.domain.District;
import org.motechproject.nms.region.domain.HealthBlock;
import org.motechproject.nms.region.domain.HealthFacility;
import org.motechproject.nms.region.domain.HealthSubFacility;
import org.motechproject.nms.region.domain.State;
import org.motechproject.nms.region.domain.Taluka;
import org.motechproject.nms.region.domain.Village;
import org.motechproject.nms.region.exception.InvalidLocationException;

import javax.validation.ConstraintViolation;
import java.util.Map;
import java.util.Set;

public final class MctsBeneficiaryUtils {

    private static final String STATE = "StateID";
    private static final String DISTRICT = "District_ID";
    private static final String TALUKA = "Taluka_ID";
    private static final String HEALTH_BLOCK = "HealthBlock_ID";
    private static final String PHC = "PHC_ID";
    private static final String SUBCENTRE = "SubCentre_ID";
    private static final String CENSUS_VILLAGE = "Village_ID";
    private static final String NON_CENSUS_VILLAGE = "SVID";

    private MctsBeneficiaryUtils() {
    }

    public static final GetInstanceByString<DateTime> DATE_BY_STRING = new GetInstanceByString<DateTime>() {
        @Override
        public DateTime retrieve(String value) {
            if (value == null) {
                return null;
            }

            DateTime referenceDate;

            try {
                DateTimeParser[] parsers = {
                        DateTimeFormat.forPattern("dd-MM-yyyy").getParser(),
                        DateTimeFormat.forPattern("dd/MM/yyyy").getParser()};
                DateTimeFormatter formatter = new DateTimeFormatterBuilder().append(null, parsers).toFormatter();

                referenceDate = formatter.parseDateTime(value);

            } catch (IllegalArgumentException e) {
                throw new CsvImportDataException(String.format("Reference date %s is invalid", value), e);
            }

            return referenceDate;
        }
    };

    public static final GetInstanceByString<Long> MSISDN_BY_STRING = new GetInstanceByString<Long>() {
        @Override
        public Long retrieve(String value) {
            if (value.length() < 10) {
                throw new NumberFormatException("Beneficiary MSISDN too short, must be at least 10 digits");
            }
            String msisdn = value.substring(value.length() - 10);

            return Long.parseLong(msisdn);
        }
    };

    public static void setLocationFields(Map<String, Object> locations, MctsBeneficiary beneficiary) throws InvalidLocationException {

        if (locations.get(STATE) == null && locations.get(DISTRICT) == null) {
            throw new InvalidLocationException("Missing mandatory state and district fields");
        }

        if (locations.get(STATE) == null) {
            throw new InvalidLocationException("Missing mandatory state field");
        }

        if (locations.get(DISTRICT) == null) {
            throw new InvalidLocationException("Missing mandatory district field");
        }

        beneficiary.setState((State) locations.get(STATE));
        beneficiary.setDistrict((District) locations.get(DISTRICT));
        beneficiary.setTaluka((Taluka) locations.get(TALUKA));
        beneficiary.setHealthBlock((HealthBlock) locations.get(HEALTH_BLOCK));
        beneficiary.setHealthFacility((HealthFacility) locations.get(PHC));
        beneficiary.setHealthSubFacility((HealthSubFacility) locations.get(SUBCENTRE));
        beneficiary.setVillage((Village) locations.get(CENSUS_VILLAGE + NON_CENSUS_VILLAGE));
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
