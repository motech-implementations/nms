package org.motechproject.nms.imi.domain;

import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;
import org.motechproject.nms.kilkari.dto.CallSummaryRecordDto;
import org.motechproject.nms.props.domain.FinalCallStatus;
import org.motechproject.nms.props.domain.RequestId;

import java.util.HashMap;

/**
 * See NMS API - 4.4.2 CDR Summary File Format
 */
@Entity(tableName = "nms_imi_csrs")
public class CallSummaryRecord {

    @Field
    private String requestId;

    @Field
    private String serviceId;

    @Field
    private Long msisdn;

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

    public Long getMsisdn() {
        return msisdn;
    }

    public void setMsisdn(Long msisdn) {
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

    public CallSummaryRecordDto toDto() {
        return new CallSummaryRecordDto(
                RequestId.fromString(requestId),
                msisdn,
                contentFileName,
                weekId,
                languageLocationCode,
                circle,
                FinalCallStatus.fromInt(finalStatus),
                new HashMap<Integer, Integer>(),
                null,
                attempts
        );
    }
}
