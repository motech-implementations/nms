package org.motechproject.nms.imi.service.impl;

import org.motechproject.nms.imi.domain.CallSummaryRecord;
import org.motechproject.nms.kilkari.exception.InvalidCallRecordDataException;
import org.motechproject.nms.props.domain.FinalCallStatus;
import org.motechproject.nms.props.domain.StatusCode;
import org.motechproject.nms.props.domain.WhatsAppOptInStatusCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class to parse a CSR CSV line to a CallSummaryRecord
 */
public final class CsrHelper {

    public static final String CSR_HEADER = "RequestId,ServiceId,Msisdn,Cli,Priority,CallFlowURL," +
            "ContentFileName,WeekId,LanguageLocationCode,Circle,FinalStatus,StatusCode,Attempts,opt_in_call_eligibility,opt_in_input";

    private static final Logger LOGGER = LoggerFactory.getLogger(CsrHelper.class);
    private static final long MIN_MSISDN = 1000000000L;
    private static final long MAX_MSISDN = 9999999999L;


    private enum FieldName {
        REQUEST_ID,
        SERVICE_ID,
        MSISDN,
        CLI,
        PRIORITY,
        CALL_FLOW_URL,
        CONTENT_FILE_NAME,
        WEEK_ID,
        LANGUAGE_LOCATION_ID,
        CIRCLE,
        FINAL_STATUS,
        STATUS_CODE,
        ATTEMPTS,
        OPT_IN_CALL_ELIGIBILITY,
        OPT_IN_INPUT,
        FIELD_COUNT,;
    }


    private CsrHelper() { }


    private static long msisdnFromString(String msisdn) {
        try {
            Long l = Long.parseLong(msisdn);
            if (l < MIN_MSISDN || l > MAX_MSISDN) {
                throw new IllegalArgumentException("MSISDN must be >= 1000000000 and <= 9999999999");
            }
            return l;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("MSISDN must be an integer", e);
        }
    }


    private static int integerFromString(String which, String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(String.format("%s must be an integer", which), e);
        }
    }

    private static int optInFromString(String which, String s) {
        try {
            if (s.equalsIgnoreCase("NULL") || s.trim().isEmpty()){
                return 5;
            }
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(String.format("%s must be an integer or null", which), e);
        }
    }
    /**
     * Validate a line from a CSR file coming from IMI could fit in into a CallSummaryRecord
     *
     * All errors will throw an IllegalArgumentException
     *
     * @param line a CSV line from a CSR file
     */
    public static void validateCsv(String line) {
        String[] fields = line.split(",");

        if (fields.length != FieldName.FIELD_COUNT.ordinal()) {
            throw new IllegalArgumentException(String.format(
                    "Invalid field count, expecting %d but received %d", FieldName.FIELD_COUNT.ordinal(),
                    fields.length));
        }
    }


    /**
     * Parse a line from a CSR file coming from IMI into a CallSummaryRecord
     *
     * CSV errors will throw an IllegalArgumentException, others a InvalidCsrException
     *
     * @param line a CSV line from a CSR file
     * @return a CallSummaryRecord
     */
    public static CallSummaryRecord csvLineToCsr(String line) {
        LOGGER.info("INSIDE csvLineToCsr");
        CallSummaryRecord csr = new CallSummaryRecord();
        String[] fields = line.split(",");

        if (fields.length != FieldName.FIELD_COUNT.ordinal()) {
            throw new IllegalArgumentException(String.format(
                    "Invalid field count, expecting %d but received %d", FieldName.FIELD_COUNT.ordinal(),
                    fields.length));
        }

        /*
         * See API 4.4.2 - CDR Summary File Format
         */

        try {
            csr.setRequestId(fields[FieldName.REQUEST_ID.ordinal()]);

            csr.setServiceId(fields[FieldName.SERVICE_ID.ordinal()]);

            csr.setMsisdn(msisdnFromString(fields[FieldName.MSISDN.ordinal()]));

            csr.setCli(fields[FieldName.CLI.ordinal()]);

            csr.setPriority(integerFromString("Priority", fields[FieldName.PRIORITY.ordinal()]));

            csr.setCallFlowUrl(fields[FieldName.CALL_FLOW_URL.ordinal()]);

            csr.setContentFileName(fields[FieldName.CONTENT_FILE_NAME.ordinal()]);

            csr.setWeekId(fields[FieldName.WEEK_ID.ordinal()]);

            csr.setLanguageLocationCode(fields[FieldName.LANGUAGE_LOCATION_ID.ordinal()]);

            csr.setCircle(fields[FieldName.CIRCLE.ordinal()]);

            csr.setFinalStatus(FinalCallStatus.fromInt(integerFromString("FinalStatus", fields[FieldName.FINAL_STATUS.ordinal()])).getValue());

            csr.setStatusCode(StatusCode.fromInt(integerFromString("StatusCode", fields[FieldName.STATUS_CODE.ordinal()])).getValue());

            csr.setAttempts(integerFromString("Attempts", fields[FieldName.ATTEMPTS.ordinal()]));
            LOGGER.info("INSIDE csvLineToCsr -- SETTING OPT_IN_CALL_ELIGIBILITY");
            csr.setOpt_in_call_eligibility(Boolean.parseBoolean(fields[FieldName.OPT_IN_CALL_ELIGIBILITY.ordinal()]));
            LOGGER.info("INSIDE csvLineToCsr -- SETTING OPT_IN_INPUT");
            csr.setOpt_in_input(WhatsAppOptInStatusCode.fromValue(optInFromString("opt_in_input", fields[FieldName.OPT_IN_INPUT.ordinal()])).toString());
            LOGGER.debug("CSR object after csvLineToCsr : {}", csr.toString());

        } catch (IllegalArgumentException e) {
            throw new InvalidCallRecordDataException(e.getMessage(), e);
        }

        return csr;
    }

    /**
     * Validate Header coming in CSR file from IMI
     *
     * @param line a CSV line from a CSR file
     *
     */
    public static void validateHeader(String line) {

        if (!(CSR_HEADER.equalsIgnoreCase(line))) {
            throw new IllegalArgumentException("Invalid CSR header");
        }
    }

}
