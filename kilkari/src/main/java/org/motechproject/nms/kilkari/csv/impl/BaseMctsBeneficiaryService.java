package org.motechproject.nms.kilkari.csv.impl;


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

public abstract class BaseMctsBeneficiaryService {

    protected static final String STATE = "StateID";
    protected static final String DISTRICT = "District_ID";
    protected static final String TALUKA = "Taluka_ID";
    protected static final String HEALTH_BLOCK = "HealthBlock_ID";
    protected static final String PHC = "PHC_ID";
    protected static final String SUBCENTRE = "SubCentre_ID";
    protected static final String CENSUS_VILLAGE = "Village_ID";
    protected static final String NON_CENSUS_VILLAGE = "SVID";

    private GetInstanceByString<DateTime> getDateByString = new GetInstanceByString<DateTime>() {
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

    public GetInstanceByString<DateTime> getDateProcessor() {
        return getDateByString;
    }


    protected void setLocationFields(Map<String, Object> locations, MctsBeneficiary beneficiary) throws InvalidLocationException {

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
        beneficiary.setPrimaryHealthCenter((HealthFacility) locations.get(PHC));
        beneficiary.setHealthSubFacility((HealthSubFacility) locations.get(SUBCENTRE));
        beneficiary.setVillage((Village) locations.get(CENSUS_VILLAGE + NON_CENSUS_VILLAGE));
    }

    protected String createErrorMessage(String message, int rowNumber) {
        return String.format("CSV instance error [row: %d]: %s", rowNumber, message);
    }

    protected String createErrorMessage(Set<ConstraintViolation<?>> violations, int rowNumber) {
        return String.format("CSV instance error [row: %d]: validation failed for instance of type %s, violations: %s",
                rowNumber, MctsBeneficiary.class.getName(), ConstraintViolationUtils.toString(violations));
    }

    protected void verify(boolean condition, String message, String... args) {
        if (!condition) {
            throw new CsvImportDataException(String.format(message, args));
        }
    }

}
