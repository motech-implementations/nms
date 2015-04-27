package org.motechproject.nms.api.web.contract;

/**
 * Request body
 *
 * 2.2.7 Set User Language Location Code API
 * IVR shall invoke this API to provide user languageLocation preference to MoTech.
 * /api/mobileacademy/languageLocationCode
 *
 * 3.2.3 Set User Language Location Code API
 * IVR shall invoke this API to set the language location code of the user in NMS database.
 * /api/mobilekunji/languageLocationCode
 *
 */
public class UserLanguageRequest {
    private Long callingNumber;
    private Long callId;
    private String languageLocationCode;

    // Necessary for Jackson
    public UserLanguageRequest() { }

    // Used in ITs only
    public UserLanguageRequest(Long callingNumber, Long callId, String languageLocationCode) {
        this.callingNumber = callingNumber;
        this.callId = callId;
        this.languageLocationCode = languageLocationCode;
    }

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

    public String getLanguageLocationCode() {
        return languageLocationCode;
    }

    public void setLanguageLocationCode(String languageLocationCode) {
        this.languageLocationCode = languageLocationCode;
    }
}
