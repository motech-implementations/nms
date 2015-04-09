package org.motechproject.nms.api.web.contract;

/**
 *
 */
public class KilkariResponseUserNewUnknownLanguage extends KilkariResponseUser {
    private String languageLocationCode;

    public KilkariResponseUserNewUnknownLanguage(String circle, String languageLocationCode) {
        super(circle);
        this.languageLocationCode = languageLocationCode;
    }

    public String getLanguageLocationCode() {
        return languageLocationCode;
    }

    public void setLanguageLocationCode(String languageLocationCode) {
        this.languageLocationCode = languageLocationCode;
    }
}
