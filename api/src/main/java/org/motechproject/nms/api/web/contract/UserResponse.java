package org.motechproject.nms.api.web.contract;

import java.util.HashSet;
import java.util.Set;

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
    private Set<String> allowedLanguageLocationCodes;

    public UserResponse() {
        this.allowedLanguageLocationCodes = new HashSet<>();
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

    public Set<String> getAllowedLanguageLocationCodes() {
        return allowedLanguageLocationCodes;
    }

    public void setAllowedLanguageLocationCodes(Set<String> allowedLanguageLocationCodes) {
        this.allowedLanguageLocationCodes = allowedLanguageLocationCodes;
    }

    @Override
    public String toString() {
        return "UserResponse{" +
                "languageLocationCode='" + languageLocationCode + '\'' +
                ", defaultLanguageLocationCode='" + defaultLanguageLocationCode + '\'' +
                ", allowedLanguageLocationCodes=" + allowedLanguageLocationCodes +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        UserResponse that = (UserResponse) o;

        if (languageLocationCode != null ? !languageLocationCode
                .equals(that.languageLocationCode) : that.languageLocationCode != null) {
            return false;
        }
        if (defaultLanguageLocationCode != null ? !defaultLanguageLocationCode
                .equals(that.defaultLanguageLocationCode) : that.defaultLanguageLocationCode != null) {
            return false;
        }
        return !(allowedLanguageLocationCodes != null ? !allowedLanguageLocationCodes
                .equals(that.allowedLanguageLocationCodes) : that.allowedLanguageLocationCodes != null);

    }

    @Override
    public int hashCode() {
        int result = languageLocationCode != null ? languageLocationCode.hashCode() : 0;
        result = 31 * result + (defaultLanguageLocationCode != null ? defaultLanguageLocationCode
                .hashCode() : 0);
        result = 31 * result + (allowedLanguageLocationCodes != null ? allowedLanguageLocationCodes
                .hashCode() : 0);
        return result;
    }
}
