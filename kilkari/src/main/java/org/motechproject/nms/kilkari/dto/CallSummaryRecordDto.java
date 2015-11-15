package org.motechproject.nms.kilkari.dto;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.codehaus.jackson.type.TypeReference;
import org.motechproject.mds.annotations.Ignore;
import org.motechproject.nms.kilkari.exception.CsrConversionException;
import org.motechproject.nms.props.domain.FinalCallStatus;
import org.motechproject.nms.props.domain.RequestId;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class CallSummaryRecordDto implements Serializable {

    private static final long serialVersionUID = -8391255985224161089L;

    /**
     * number of times the call was attempted (but failed)
     */
    private Integer callAttempts;

    @Ignore
    private static final String CALL_ATTEMPTS = "callAttempts";

    /**
     * number of seconds that the message was played (I assume that means it'll always be <= message duration)
     */
    private Integer secondsPlayed;

    @Ignore
    private static final String SECONDS_PLAYED = "secondsPlayed";

    /**
     * number of times a call failed for a specific reason
     */
    private Map<Integer, Integer> statusStats;

    @Ignore
    private static final String STATUS_STATS = "statusStats";

    /**
     * Ultimately, what's the status of this call?
     */
    private FinalCallStatus finalStatus;

    @Ignore
    private static final String FINAL_STATUS = "finalStatus";

    /**
     * calling circle
     */
    private String circle;

    @Ignore
    private static final String CIRCLE = "circle";

    /**
     * language location code
     */
    private String languageLocationCode;

    @Ignore
    private static final String LANGUAGE_LOCATION_CODE = "languageLocationCode";

    /**
     * filename of the message that was played for this call
     */
    private String contentFileName;

    @Ignore
    private static final String CONTENT_FILE_NAME = "contentFileName";

    /**
     * phone number
     */
    private Long msisdn;

    @Ignore
    private static final String MSISDN = "msisdn";

    /**
     * id of the week (within the message pack) that this call corresponds to
     */
    private String weekId;

    @Ignore
    private static final String WEEK_ID = "weekId";

    /**
     * unique id for this call: obd timestamp + subscription id
     */
    private RequestId requestId;

    @Ignore
    private static final String REQUEST_ID = "requestId";

    public CallSummaryRecordDto() { }

    public CallSummaryRecordDto(RequestId requestId, Long msisdn, // NO CHECKSTYLE More than 7 parameters
                             String contentFileName, String weekId, String languageLocationCode, String circle,
                             FinalCallStatus finalStatus, Map<Integer, Integer> statusStats,
                             Integer secondsPlayed, Integer callAttempts) {
        this.requestId = requestId;
        this.msisdn = msisdn;
        this.contentFileName = contentFileName;
        this.weekId = weekId;
        this.languageLocationCode = languageLocationCode;
        this.circle = circle;
        this.finalStatus = finalStatus;
        this.statusStats = statusStats;
        this.secondsPlayed = secondsPlayed;
        this.callAttempts = callAttempts;
    }

    public Integer getCallAttempts() {
        return callAttempts;
    }

    public void setCallAttempts(Integer callAttempts) {
        this.callAttempts = callAttempts;
    }

    public Integer getSecondsPlayed() {
        return secondsPlayed;
    }

    public void setSecondsPlayed(Integer secondsPlayed) {
        this.secondsPlayed = secondsPlayed;
    }

    public Map<Integer, Integer> getStatusStats() {
        return statusStats;
    }

    public void setStatusStats(Map<Integer, Integer> statusStats) {
        this.statusStats = statusStats;
    }

    public FinalCallStatus getFinalStatus() {
        return finalStatus;
    }

    public void setFinalStatus(FinalCallStatus finalStatus) {
        this.finalStatus = finalStatus;
    }

    public String getCircle() {
        return circle;
    }

    public void setCircle(String circle) {
        this.circle = circle;
    }

    public String getLanguageLocationCode() {
        return languageLocationCode;
    }

    public void setLanguageLocationCode(String languageLocationCode) {
        this.languageLocationCode = languageLocationCode;
    }

    public String getContentFileName() {
        return contentFileName;
    }

    public void setContentFileName(String contentFileName) {
        this.contentFileName = contentFileName;
    }

    public Long getMsisdn() {
        return msisdn;
    }

    public void setMsisdn(Long msisdn) {
        this.msisdn = msisdn;
    }

    public String getWeekId() {
        return weekId;
    }

    public void setWeekId(String weekId) {
        this.weekId = weekId;
    }

    public RequestId getRequestId() {
        return requestId;
    }

    public void setRequestId(RequestId requestId) {
        this.requestId = requestId;
    }


    @Ignore
    private static String mapToJson(Map<Integer, Integer> map) {
        ObjectWriter writer = new ObjectMapper().writer();

        try {
            return writer.writeValueAsString(map);
        } catch (JsonGenerationException | JsonMappingException je) {
            throw new CsrConversionException(je);
        } catch (IOException io) {
            throw new CsrConversionException(io);
        }
    }


    @Ignore
    private static Map<Integer, Integer> jsonToMap(String json) {
        ObjectMapper mapper = new ObjectMapper();

        try {
            return mapper.readValue(json, new TypeReference<Map<Integer, Integer>>() {
            });
        } catch (JsonGenerationException | JsonMappingException je) {
            throw new CsrConversionException(je);
        } catch (IOException io) {
            throw new CsrConversionException(io);
        }
    }


    @Ignore
    public static CallSummaryRecordDto fromParams(Map<String, Object> params) {
        CallSummaryRecordDto csr;
        csr = new CallSummaryRecordDto(
            RequestId.fromString((String) params.get(REQUEST_ID)),
            (Long) params.get(MSISDN),
            (String) params.get(CONTENT_FILE_NAME),
            (String) params.get(WEEK_ID),
            (String) params.get(LANGUAGE_LOCATION_CODE),
            (String) params.get(CIRCLE),
            FinalCallStatus.fromInt((Integer) params.get(FINAL_STATUS)),
            jsonToMap((String) params.get(STATUS_STATS)),
            (Integer) params.get(SECONDS_PLAYED),
            (Integer) params.get(CALL_ATTEMPTS)
        );
        return csr;
    }


    @Ignore
    public static Map<String, Object> toParams(CallSummaryRecordDto csr) {
        Map<String, Object> params = new HashMap<>();
        params.put(CALL_ATTEMPTS, csr.callAttempts);
        params.put(SECONDS_PLAYED, csr.secondsPlayed);
        params.put(STATUS_STATS, mapToJson(csr.statusStats));
        params.put(FINAL_STATUS, csr.finalStatus.getValue());
        params.put(CIRCLE, csr.circle);
        params.put(LANGUAGE_LOCATION_CODE, csr.languageLocationCode);
        params.put(CONTENT_FILE_NAME, csr.contentFileName);
        params.put(MSISDN, csr.msisdn);
        params.put(WEEK_ID, csr.weekId);
        params.put(REQUEST_ID, csr.requestId.toString());
        return params;
    }


    @Override
    public String toString() {
        return "CallSummaryRecordDto{" +
                "requestId='" + requestId + '\'' +
                ", msisdn=" + msisdn +
                ", finalStatus=" + finalStatus +
                '}';
    }
}
