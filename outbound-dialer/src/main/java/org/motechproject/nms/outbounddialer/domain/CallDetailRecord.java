package org.motechproject.nms.outbounddialer.domain;

import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;

@Entity(tableName = "nms_obd_cdrs")
public class CallDetailRecord {
    @Field
    private String requestId;
    @Field
    private String serviceId;
    @Field
    private String msisdn;
    @Field
    private String cli;
    @Field
    private Integer priority;
    @Field
    private String callFlowUrl;
    @Field
    private String contentFileName;
    @Field
    private String weekId;
    @Field
    private String languageLocationCode;
    @Field
    private String circle;
    @Field
    private Integer finalStatus;
    @Field
    private Integer statusCode;
    @Field
    private Integer attempts;

    static final int NUMBER_OF_FIELDS = 13;

    public CallDetailRecord() { }

    public CallDetailRecord(String requestId, String serviceId, String msisdn, String cli, Integer priority,
                            String callFlowUrl, String contentFileName, String weekId, String languageLocationCode,
                            String circle, Integer finalStatus, Integer statusCode, Integer attempts) {
        this.requestId = requestId;
        this.serviceId = serviceId;
        this.msisdn = msisdn;
        this.cli = cli;
        this.priority = priority;
        this.callFlowUrl = callFlowUrl;
        this.contentFileName = contentFileName;
        this.weekId = weekId;
        this.languageLocationCode = languageLocationCode;
        this.circle = circle;
        this.finalStatus = finalStatus;
        this.statusCode = statusCode;
        this.attempts = attempts;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getMsisdn() {
        return msisdn;
    }

    public void setMsisdn(String msisdn) {
        this.msisdn = msisdn;
    }

    public String getCli() {
        return cli;
    }

    public void setCli(String cli) {
        this.cli = cli;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public String getCallFlowUrl() {
        return callFlowUrl;
    }

    public void setCallFlowUrl(String callFlowUrl) {
        this.callFlowUrl = callFlowUrl;
    }

    public String getContentFileName() {
        return contentFileName;
    }

    public void setContentFileName(String contentFileName) {
        this.contentFileName = contentFileName;
    }

    public String getWeekId() {
        return weekId;
    }

    public void setWeekId(String weekId) {
        this.weekId = weekId;
    }

    public String getLanguageLocationCode() {
        return languageLocationCode;
    }

    public void setLanguageLocationCode(String languageLocationCode) {
        this.languageLocationCode = languageLocationCode;
    }

    public String getCircle() {
        return circle;
    }

    public void setCircle(String circle) {
        this.circle = circle;
    }

    public Integer getFinalStatus() {
        return finalStatus;
    }

    public void setFinalStatus(Integer finalStatus) {
        this.finalStatus = finalStatus;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

    public Integer getAttempts() {
        return attempts;
    }

    public void setAttempts(Integer attempts) {
        this.attempts = attempts;
    }

    public static CallDetailRecord fromLine(String line) {
        String[] fields = line.split(",");
        if (fields.length != NUMBER_OF_FIELDS) {
            throw new IllegalStateException(String.format("Wrong number of fields, expecting %d but seeing %d",
                    NUMBER_OF_FIELDS,
                    fields.length));
        }

        //NOTE: the Integer.parseInt calls below may throw a NumberFormatException
        return new CallDetailRecord(fields[0], fields[1], fields[2], fields[3], Integer.parseInt(fields[4]), fields[5],
                fields[6], fields[7], fields[8], fields[9], Integer.parseInt(fields[10]),
                Integer.parseInt(fields[11]), Integer.parseInt(fields[12]));
    }
}
