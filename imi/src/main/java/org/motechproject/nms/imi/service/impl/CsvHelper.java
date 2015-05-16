package org.motechproject.nms.imi.service.impl;

import org.joda.time.DateTime;
import org.motechproject.nms.kilkari.dto.CallDetailRecordDto;
import org.motechproject.nms.props.domain.CallDisconnectReason;
import org.motechproject.nms.props.domain.RequestId;
import org.motechproject.nms.props.domain.StatusCode;

/**
 * Helper class to parse a CSV line to a CallSummaryRecord (and vice versa, for use in ITs)
 */
public final class CsvHelper {

    private static final long MIN_MSISDN = 1000000000L;
    private static final long MAX_MSISDN = 9999999999L;
    private static final int MILLIS_PER_SEC = 1000;

    private enum FieldName {
        REQUEST_ID,
        MSISDN,
        CALL_ID,
        ATTEMPT_NO,
        CALL_START_TIME,
        CALL_ANSWER_TIME,
        CALL_END_TIME,
        CALL_DURATION_IN_PULSE,
        CALL_STATUS,
        LANGUAGE_LOCATION_ID,
        CONTENT_FILE,
        MSG_PLAY_START_TIME,
        MSG_PLAY_END_TIME,
        CIRCLE_ID,
        OPERATOR_ID,
        PRIORITY,
        CALL_DISCONNECT_REASON,
        WEEK_ID,
        FIELD_COUNT;
    }


    private CsvHelper() { }


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


    private static long longFromString(String which, String s) {
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(String.format("%s must be an integer", which), e);
        }

    }


    private static int integerFromString(String which, String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(String.format("%s must be an integer", which), e);
        }

    }


    private static Integer calculateMsgPlayDuration(String msgPlayStartTime, String msgPlayEndTime) {
        Long start = longFromString("MsgPlayStartTime", msgPlayStartTime);
        Long end = longFromString("MsgPlayEndTime", msgPlayEndTime);

        if (end < start) {
            throw new IllegalArgumentException("MsgPlayEndTime cannot be before MsgPlayStartTime");
        }

        if ((end - start) > Integer.MAX_VALUE) {
            throw new IllegalArgumentException(
                    "The difference between MsgPlayEndTime and MsgPlayStartTime is too large");
        }

        return ((int) (end - start));
    }


    /**
     * Take what we need from an IMI CDRDetail line to make our CDR DTO
     *
     * All errors will throw an IllegalArgumentException
     *
     * @param line a CSV line from a CDR Detail File from IMI
     * @return a CallDetailRecordDto
     */
    public static CallDetailRecordDto csvLineToCdr(String line) {
        CallDetailRecordDto cdr = new CallDetailRecordDto();
        String[] fields = line.split(",");

        if (fields.length != FieldName.FIELD_COUNT.ordinal()) {
            throw new IllegalArgumentException(String.format(
                    "Invalid field count, expecting %d but received %d", FieldName.FIELD_COUNT, fields.length));
        }


        /*
         * See API 4.4.3 - CDR Detail File Format
         */

        cdr.setRequestId(RequestId.fromString(fields[FieldName.REQUEST_ID.ordinal()]));

        cdr.setMsisdn(msisdnFromString(fields[FieldName.MSISDN.ordinal()]));

        cdr.setCallAnswerTime(new DateTime(longFromString("CallAnswerTime",
                fields[FieldName.CALL_ANSWER_TIME.ordinal()])));

        cdr.setMsgPlayDuration(calculateMsgPlayDuration(fields[FieldName.MSG_PLAY_START_TIME.ordinal()],
                fields[FieldName.MSG_PLAY_END_TIME.ordinal()]));

        cdr.setStatusCode(StatusCode.fromInt(integerFromString("CallStatus",
                fields[FieldName.CALL_STATUS.ordinal()])));

        cdr.setLanguageLocationId(fields[FieldName.LANGUAGE_LOCATION_ID.ordinal()]);

        cdr.setContentFile(fields[FieldName.CONTENT_FILE.ordinal()]);

        cdr.setCircleId(fields[FieldName.CIRCLE_ID.ordinal()]);

        cdr.setOperatorId(fields[FieldName.OPERATOR_ID.ordinal()]);

        cdr.setCallDisconnectReason(CallDisconnectReason.fromInt(integerFromString("CallDisconnectReason",
                fields[FieldName.CALL_DISCONNECT_REASON.ordinal()])));

        cdr.setWeekId(fields[FieldName.WEEK_ID.ordinal()]);

        return cdr;
    }

    public static String csvLineFromCdr(CallDetailRecordDto cdr) {
        StringBuilder sb = new StringBuilder();

        //REQUEST_ID,
        sb.append(cdr.getRequestId().toString());
        sb.append(',');

        //MSISDN,
        sb.append(cdr.getMsisdn());
        sb.append(',');

        //CALL_ID,
        sb.append("xxx");
        sb.append(',');

        //ATTEMPT_NO,
        sb.append(1);
        sb.append(',');

        //CALL_START_TIME,
        sb.append(1);
        sb.append(',');

        //CALL_ANSWER_TIME,
        sb.append(cdr.getCallAnswerTime().getMillis() / MILLIS_PER_SEC);
        sb.append(',');

        //CALL_END_TIME,
        sb.append(1);
        sb.append(',');

        //CALL_DURATION_IN_PULSE,
        sb.append(1);
        sb.append(',');

        //CALL_STATUS,
        sb.append(cdr.getStatusCode().getValue());
        sb.append(',');

        //LANGUAGE_LOCATION_ID,
        sb.append(cdr.getLanguageLocationId());
        sb.append(',');

        //CONTENT_FILE,
        sb.append(cdr.getContentFile());
        sb.append(',');

        //MSG_PLAY_START_TIME,
        sb.append(1);
        sb.append(',');

        //MSG_PLAY_END_TIME,
        sb.append(1 + cdr.getMsgPlayDuration());
        sb.append(',');

        //CIRCLE_ID,
        sb.append(cdr.getCircleId());
        sb.append(',');

        //OPERATOR_ID,
        sb.append(cdr.getOperatorId());
        sb.append(',');

        //PRIORITY,
        sb.append(0);
        sb.append(',');

        //CALL_DISCONNECT_REASON,
        sb.append(cdr.getCallDisconnectReason().getValue());
        sb.append(',');

        //WEEK_ID,
        sb.append(cdr.getWeekId());


        return sb.toString();
    }

}
