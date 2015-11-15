package org.motechproject.nms.imi.service.impl;

import org.motechproject.nms.imi.domain.CallSummaryRecord;
import org.motechproject.nms.imi.exception.InvalidCsrException;
import org.motechproject.nms.props.domain.StatusCode;

/**
 * Helper class to parse a CSR CSV line to a CallSummaryRecord
 */
public final class CsrHelper {

    public static final String CSR_HEADER = "RequestId,ServiceId,Msisdn,Cli,Priority,CallFlowURL," +
            "ContentFileName,WeekId,LanguageLocationCode,Circle,FinalStatus,StatusCode,Attempts";


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
        FIELD_COUNT;
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

            csr.setFinalStatus(integerFromString("FinalStatus", fields[FieldName.FINAL_STATUS.ordinal()]));

            csr.setStatusCode(StatusCode.fromInt(integerFromString("StatusCode", fields[FieldName.STATUS_CODE.ordinal()])).getValue());

            csr.setAttempts(integerFromString("Attempts", fields[FieldName.ATTEMPTS.ordinal()]));
        } catch (IllegalArgumentException e) {
            throw new InvalidCsrException(e);
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
