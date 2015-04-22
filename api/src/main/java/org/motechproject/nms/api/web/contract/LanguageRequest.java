package org.motechproject.nms.api.web.contract;

public class LanguageRequest {
    private String callingNumber;
    private String callId;
    private String languageLocationCode;

    // Necessary for Jackson
    public LanguageRequest() { }

    // Used in ITs only
    public LanguageRequest(String callingNumber, String callId, String languageLocationCode) {
        this.callingNumber = callingNumber;
        this.callId = callId;
        this.languageLocationCode = languageLocationCode;
    }

    public String getCallingNumber() {
        return callingNumber;
    }

    public void setCallingNumber(String callingNumber) {
        this.callingNumber = callingNumber;
    }

    public String getCallId() {
        return callId;
    }

    public void setCallId(String callId) {
        this.callId = callId;
    }

    public String getLanguageLocationCode() {
        return languageLocationCode;
    }

    public void setLanguageLocationCode(String languageLocationCode) {
        this.languageLocationCode = languageLocationCode;
    }
}
