package org.motechproject.nms.api.web.contract;

import java.util.HashSet;

/**
 *
 */
public abstract class ResponseUser {
    protected String languageLocationCode;
    protected String defaultLanguageLocationCode;

    public ResponseUser() {}

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
