package org.motechproject.nms.imi.service.impl;

import org.motechproject.nms.imi.domain.WhatsAppOptCsr;
import org.motechproject.nms.imi.domain.WhatsAppOptSMSCsr;
import org.motechproject.nms.kilkari.domain.WhatsAppMessageStatus;
import org.motechproject.nms.kilkari.exception.InvalidCallRecordDataException;

import java.util.HashMap;
import java.util.Map;

//external_id	Urn	ContentFileName	Week_id	Message_status_timestamp	Message_Statu
public class WhatAppCsrHelper {
    public static final String WHATSAPP_CSR_HEADER = "external_id,urn,content_file_name,week_id,PREFERRED_LANGUAGE,state_code,message_status_timestamp,message_status";
    private static final long MIN_URN = 1000000000L;
    private static final long MAX_URN = 9999999999L;

    private enum FieldName {
        EXTERNAL_ID,
        URN,
        CONTENT_FILE_NAME,
        WEEK_ID,
        PREFERRED_LANGUAGE,
        STATE_CODE,
        MESSAGE_STATUS_TIMESTAMP,
        MESSAGE_STATUS,
        FIELD_COUNT

    }

    public WhatAppCsrHelper() {
    }

    private static long msisdnFromString(String msisdn) {
        try {
            Long l = Long.parseLong(msisdn);
            if (l < MIN_URN || l > MAX_URN) {
                throw new IllegalArgumentException("URN must be >= 1000000000 and <= 9999999999");
            }
            return l;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("URN must be an integer", e);
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
     * Validate a line from a WhatsAppSMS CSR file coming from IMI could fit in into a CallSummaryRecord
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
    public static WhatsAppOptCsr csvLineToWhatsAppCsr(String line) {
        WhatsAppOptCsr csr = new WhatsAppOptCsr();
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
            csr.setExternalId(fields[FieldName.EXTERNAL_ID.ordinal()]);
            csr.setUrn(msisdnFromString(fields[FieldName.URN.ordinal()].substring(2)));
            csr.setContentFileName(fields[FieldName.CONTENT_FILE_NAME.ordinal()]);
            csr.setWeekId(fields[FieldName.WEEK_ID.ordinal()]);
            csr.setPreferredLanguage(fields[FieldName.PREFERRED_LANGUAGE.ordinal()]);
            csr.setStateCode(Long.parseLong(fields[FieldName.STATE_CODE.ordinal()]));
            csr.setMessageStatusTimestamp(fields[FieldName.MESSAGE_STATUS_TIMESTAMP.ordinal()]);
            csr.setMessageStatus(WhatsAppMessageStatus.valueOf(fields[FieldName.MESSAGE_STATUS.ordinal()]));


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

        if (!(WHATSAPP_CSR_HEADER.equalsIgnoreCase(line))) {
            throw new IllegalArgumentException("Invalid CSR header");
        }
    }

    public static Map<String, Object> toParams(WhatsAppOptSMSCsr csr) {
        Map<String, Object> params = new HashMap<>();
        params.put("circleId", csr.getCircleId());
        params.put("contentFile", csr.getContentFile());
        params.put("languageLocationId", csr.getContentFile());
        params.put("msisdn", csr.getMsisdn());
        params.put("operatorId", csr.getOperatorId());
        params.put("requestId", csr.getRequestId());
        params.put("smsSent", csr.getSmsSent());
        params.put("response", csr.getResponse());
        params.put("creationDate", csr.getCreationDate());
        params.put("modificationDate", csr.getModificationDate());
        return params;
    }
}
