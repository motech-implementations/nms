package org.motechproject.nms.kilkari.dto;

import org.joda.time.DateTime;
import org.motechproject.nms.kilkari.domain.WhatsAppMessageStatus;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class WhatsAppOptCsrDto implements Serializable {


    private String externalId;


    private Long urn;


    private String contentFileName;


    private String weekId;


    private String messageStatusTimestamp;


    private WhatsAppMessageStatus messageStatus;


    public WhatsAppOptCsrDto() {
    }

    public WhatsAppOptCsrDto(String externalId, Long urn, String contentFileName, String weekId, String messageStatusTimestamp, WhatsAppMessageStatus messageStatus) {
        this.externalId = externalId;
        this.urn = urn;
        this.contentFileName = contentFileName;
        this.weekId = weekId;
        this.messageStatusTimestamp = messageStatusTimestamp;
        this.messageStatus = messageStatus;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public Long getUrn() {
        return urn;
    }

    public void setUrn(Long urn) {
        this.urn = urn;
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

    public String getMessageStatusTimestamp() {
        return messageStatusTimestamp;
    }

    public void setMessageStatusTimestamp(String messageStatusTimestamp) {
        this.messageStatusTimestamp = messageStatusTimestamp;
    }

    public WhatsAppMessageStatus getMessageStatus() {
        return messageStatus;
    }

    public void setMessageStatus(WhatsAppMessageStatus messageStatus) {
        this.messageStatus = messageStatus;
    }

    public static Map<String, Object> toParams(WhatsAppOptCsrDto csr) {
        Map<String, Object> params = new HashMap<>();
        params.put("externalId", csr.getExternalId());
        params.put("urn", csr.getUrn());
        params.put("contentFileName", csr.getContentFileName());
        params.put("weekId", csr.getWeekId());
        params.put("messageStatusTimestamp", csr.getMessageStatusTimestamp());
        params.put("messageStatus", csr.getMessageStatus());
        return params;
    }

    public static WhatsAppOptCsrDto fromParams(Map<String, Object> params) {
        WhatsAppOptCsrDto csr;
        csr = new WhatsAppOptCsrDto(
                (String) params.get("externalId"),
                (Long) params.get("urn"),
                (String) params.get("contentFileName"),
                (String) params.get("weekId"),
                (String) params.get("messageStatusTimestamp"),
                (WhatsAppMessageStatus) params.get("messageStatus")
        );
        return csr;
    }
}
