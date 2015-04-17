package org.motechproject.nms.api.web.contract;

/**
 *
 */
public abstract class ResponseUser {
    private Integer languageLocationCode;
    private Integer defaultLanguageLocationCode;

    public ResponseUser() { }

    public ResponseUser(Integer languageLocationCode) {
        this.languageLocationCode = languageLocationCode;
    }

    public Integer getLanguageLocationCode() {
        return languageLocationCode;
    }

    public void setLanguageLocationCode(Integer languageLocationCode) {
        this.languageLocationCode = languageLocationCode;
    }

    public Integer getDefaultLanguageLocationCode() {
        return defaultLanguageLocationCode;
    }

    public void setDefaultLanguageLocationCode(Integer defaultLanguageLocationCode) {
        this.defaultLanguageLocationCode = defaultLanguageLocationCode;
    }
}
