package org.motechproject.nms.region.domain;

import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;
import org.motechproject.mds.domain.MdsEntity;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.Unique;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * Table holds the national default language location code.
 */
@Entity(tableName = "nms_national_default_language_location")
public class NationalDefaultLanguageLocation extends MdsEntity {
    @Field
    @Unique
    @Column(allowsNull = "false", defaultValue = "0")
    @Max(0) @Min(0)
    private int code = 0;

    @Field
    @NotNull
    @Column(allowsNull = "false")
    private LanguageLocation languageLocation;

    public NationalDefaultLanguageLocation(LanguageLocation languageLocation) {
        this.languageLocation = languageLocation;
    }

    public int getCode() {
        return code;
    }

    public LanguageLocation getLanguageLocation() {
        return languageLocation;
    }

    public void setLanguageLocation(LanguageLocation languageLocation) {
        this.languageLocation = languageLocation;
    }
}
