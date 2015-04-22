package org.motechproject.nms.api.web.contract.mobileAcademy;

/**
 * Request object to save language location code for a user
 */
public class LanguageLocationCodeRequest {

    private String callingNumber;

    private String callId;

    private int languageLocationCode;

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

    public int getLanguageLocationCode() {
        return languageLocationCode;
    }

    public void setLanguageLocationCode(int languageLocationCode) {
        this.languageLocationCode = languageLocationCode;
    }
}
