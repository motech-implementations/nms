package org.motechproject.nms.api.web.contract.mobileAcademy;

/**
 * Request object to save language location code for a user
 */
public class LanguageLocationCodeRequest {

    private Long callingNumber;

    private Long callId;

    private Integer languageLocationCode;

    public Long getCallingNumber() {
        return callingNumber;
    }

    public void setCallingNumber(Long callingNumber) {
        this.callingNumber = callingNumber;
    }

    public Long getCallId() {
        return callId;
    }

    public void setCallId(Long callId) {
        this.callId = callId;
    }

    public int getLanguageLocationCode() {
        return languageLocationCode;
    }

    public void setLanguageLocationCode(Integer languageLocationCode) {
        this.languageLocationCode = languageLocationCode;
    }
}
