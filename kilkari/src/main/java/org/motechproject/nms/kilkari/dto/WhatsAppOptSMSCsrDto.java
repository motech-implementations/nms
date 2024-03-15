package org.motechproject.nms.kilkari.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.DateDeserializers;
import org.joda.time.DateTime;
import org.motechproject.nms.kilkari.domain.JodaTimeModule;
import org.motechproject.nms.kilkari.domain.WhatsAppOptInResponse;

import java.io.Serializable;

import java.util.HashMap;
import java.util.Map;

public class WhatsAppOptSMSCsrDto implements Serializable {

    private String circleId;

    private String contentFile;

    private String languageLocationId;

    private Long msisdn;

    private String operatorId;

    private String requestId;


    private Boolean smsSent;

    private WhatsAppOptInResponse response;

    @JsonDeserialize(using = JodaTimeModule.DateTimeDeserializer.class)
    private DateTime creationDate;


    @JsonDeserialize(using = JodaTimeModule.DateTimeDeserializer.class)
    private DateTime modificationDate;

    public WhatsAppOptSMSCsrDto() {
    }

    public WhatsAppOptSMSCsrDto(String circleId, String contentFile, String languageLocationId, Long msisdn, String operatorId, String requestId, Boolean smsSent, WhatsAppOptInResponse response, DateTime creationDate, DateTime modificationDate) {
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

    public static Map<String, Object> toParams(WhatsAppOptSMSCsrDto csr) {
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

    public static WhatsAppOptSMSCsrDto fromParams(Map<String, Object> params) {
        WhatsAppOptSMSCsrDto csr;
        csr = new WhatsAppOptSMSCsrDto(
                (String) params.get("circleId"),
                (String) params.get("contentFile"),
                (String) params.get("languageLocationId"),
                (Long) params.get("msisdn"),
                (String) params.get("operatorId"),
                (String) params.get("requestId"),
                (Boolean) params.get("smsSent"),
                (WhatsAppOptInResponse) params.get("response"),
                (DateTime) params.get("creationDate"),
                (DateTime) params.get("modificationDate")
        );
        return csr;
    }

    @Override
    public String toString() {
        return "WhatsAppOptSMSCsrDto{" +
                "circleId='" + circleId + '\'' +
                ", contentFile='" + contentFile + '\'' +
                ", languageLocationId='" + languageLocationId + '\'' +
                ", msisdn=" + msisdn +
                ", operatorId='" + operatorId + '\'' +
                ", requestId='" + requestId + '\'' +
                ", smsSent=" + smsSent +
                ", response=" + response +
                ", creationDate=" + creationDate +
                ", modificationDate=" + modificationDate +
                '}';
    }
}
