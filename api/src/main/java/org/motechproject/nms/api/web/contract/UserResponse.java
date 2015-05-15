package org.motechproject.nms.api.web.contract;

import java.util.ArrayList;
import java.util.List;

/**
 *  Response body - base class extended by KilkariUserResponse and FlwUserResponse
 *
 * 2.2.1 Get User Details API
 * IVR shall invoke this API when to retrieve details specific to the user identified by callingNumber.
 * In case user specific details are not available in the database, the API will attempt to load system
 * defaults based on the operator and circle provided.
 * /api/mobileacademy/user?callingNumber=9999999900&operator=A&circle=AP&callId=123456789012345
 *
 * 3.2.1 Get User Details API
 * IVR shall invoke this API when to retrieve details specific to the user identified by callingNumber.
 * In case user specific details are not available in the database, the API will attempt to load system
 * defaults based on the operator and circle provided.
 * /api/mobilekunji/user?callingNumber=9999999900&operator=A&circle=AP&callId=234000011111111
 *
 * 4.2.1 Get Subscriber Details API
 * IVR shall invoke this API to get the details of the beneficiary identified by the ‘callingNumber’.
 * /api/kilkari/user?callingNumber=9999999900&operator=A&circle=AP&callId=123456789123456
 *
 */
public abstract class UserResponse {
    private String languageLocationCode;
    private String defaultLanguageLocationCode;
    private List<String> allowedLanguageLocationCodes;

    public UserResponse() {
        this.allowedLanguageLocationCodes = new ArrayList<>();
    }

    public UserResponse(String languageLocationCode) {
        this.languageLocationCode = languageLocationCode;
    }

    public String getLanguageLocationCode() {
        return languageLocationCode;
    }

    public void setLanguageLocationCode(String languageLocationCode) {
        this.languageLocationCode = languageLocationCode;
    }

    public String getDefaultLanguageLocationCode() {
        return defaultLanguageLocationCode;
    }

    public void setDefaultLanguageLocationCode(String defaultLanguageLocationCode) {
        this.defaultLanguageLocationCode = defaultLanguageLocationCode;
    }

    public List<String> getAllowedLanguageLocationCodes() {
        return allowedLanguageLocationCodes;
    }

    public void setAllowedLanguageLocationCodes(List<String> allowedLanguageLocationCodes) {
        this.allowedLanguageLocationCodes = allowedLanguageLocationCodes;
    }
}
