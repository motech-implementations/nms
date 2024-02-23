package org.motechproject.nms.kilkari.domain;

import org.joda.time.DateTime;
import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;

import java.util.Date;

@Entity(tableName = "nms_imi_waos")
public class WhatsAppOptSMS {
    @Field
    private String circleId;
    @Field
    private String contentFile;
    @Field
    private String languageLocationId;
    @Field
    private Long msisdn;
    @Field
    private String operatorId;
    @Field
    private String requestId;
    @Field
    private Boolean smsSent;
    @Field
    private WhatsAppOptInResponse response;

    public WhatsAppOptSMS() {
    }

    public WhatsAppOptSMS(String circleId, String contentFile, String languageLocationId, Long msisdn, String operatorId, String requestId, Boolean smsSent, WhatsAppOptInResponse response, DateTime creationDate, DateTime modificationDate) {
        this.circleId = circleId;
        this.contentFile = contentFile;
        this.languageLocationId = languageLocationId;
        this.msisdn = msisdn;
        this.operatorId = operatorId;
        this.requestId = requestId;
        this.smsSent = smsSent;
        this.response = response;
        this.creationDate = creationDate;
        this.modificationDate = modificationDate;
    }

    @Field
    private DateTime creationDate;

    @Field
    private DateTime modificationDate;

    public DateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(DateTime creationDate) {
        this.creationDate = creationDate;
    }

    public DateTime getModificationDate() {
        return modificationDate;
    }

    public void setModificationDate(DateTime modificationDate) {
        this.modificationDate = modificationDate;
    }

    public String getCircleId() {
        return circleId;
    }

    public void setCircleId(String circleId) {
        this.circleId = circleId;
    }

    public String getContentFile() {
        return contentFile;
    }

    public void setContentFile(String contentFile) {
        this.contentFile = contentFile;
    }

    public String getLanguageLocationId() {
        return languageLocationId;
    }

    public void setLanguageLocationId(String languageLocationId) {
        this.languageLocationId = languageLocationId;
    }

    public Long getMsisdn() {
        return msisdn;
    }

    public void setMsisdn(Long msisdn) {
        this.msisdn = msisdn;
    }

    public String getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(String operatorId) {
        this.operatorId = operatorId;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public Boolean getSmsSent() {
        return smsSent;
    }

    public void setSmsSent(Boolean smsSent) {
        this.smsSent = smsSent;
    }

    public WhatsAppOptInResponse getResponse() {
        return response;
    }

    public void setResponse(WhatsAppOptInResponse response) {
        this.response = response;
    }
}

