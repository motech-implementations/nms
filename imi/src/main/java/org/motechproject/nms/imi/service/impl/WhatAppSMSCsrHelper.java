package org.motechproject.nms.imi.service.impl;

import org.joda.time.DateTime;
import org.motechproject.nms.imi.domain.WhatsAppOptSMSCsr;
import org.motechproject.nms.kilkari.domain.WhatsAppOptInResponse;
import org.motechproject.nms.kilkari.exception.InvalidCallRecordDataException;
import java.util.HashMap;
import java.util.Map;

public class WhatAppSMSCsrHelper {
    public static final String WHATSAPP_SMS_CSR_HEADER = "CircleId,ContentFile,LanguageLocationId,Msisdn,RequestId,Response";
    private static final long MIN_MSISDN = 1000000000L;
    private static final long MAX_MSISDN = 9999999999L;

    private enum FieldName {
        CIRCLE_ID,
        CONTENT_FILE,
        LANGUAGE_LOCATION_ID,
        MSISDN,
        REQUEST_ID,
        RESPONSE,
        FIELD_COUNT

    }

    public WhatAppSMSCsrHelper() {
    }

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
    public static WhatsAppOptSMSCsr csvLineToWhatsAppSMSCsr(String line) {
        WhatsAppOptSMSCsr csr = new WhatsAppOptSMSCsr();
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
            csr.setCircleId(fields[FieldName.CIRCLE_ID.ordinal()]);
            csr.setContentFile(fields[FieldName.CONTENT_FILE.ordinal()]);
            csr.setLanguageLocationId(fields[FieldName.LANGUAGE_LOCATION_ID.ordinal()]);
            csr.setMsisdn(msisdnFromString(fields[FieldName.MSISDN.ordinal()]));
            //Operator Id need to provided.
            csr.setOperatorId("");
            csr.setRequestId(fields[FieldName.REQUEST_ID.ordinal()]);
            csr.setSmsSent(true);
            csr.setResponse(WhatsAppOptInResponse.fromValue(integerFromString("Response",fields[FieldName.RESPONSE.ordinal()])));
            csr.setCreationDate(new DateTime());
            csr.setModificationDate(new DateTime());

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

        if (!(WHATSAPP_SMS_CSR_HEADER.equalsIgnoreCase(line))) {
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
