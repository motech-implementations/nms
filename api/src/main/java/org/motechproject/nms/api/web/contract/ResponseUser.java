package org.motechproject.nms.api.web.contract;

/**
 *
 */
public abstract class ResponseUser {
    private String languageLocationCode;
    private String defaultLanguageLocationCode;

    public ResponseUser() { }

    public ResponseUser(String languageLocationCode) {
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
}
