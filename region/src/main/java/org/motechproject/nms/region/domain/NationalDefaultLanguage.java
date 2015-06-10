package org.motechproject.nms.region.domain;

import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;
import org.motechproject.mds.domain.MdsEntity;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Unique;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * Table holds the national default language location code.
 */
@Entity(tableName = "nms_national_default_language")
@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable = "true")
public class NationalDefaultLanguage extends MdsEntity {
    @Field
    @Unique
    @Column(allowsNull = "false", defaultValue = "0")
    @Max(0) @Min(0)
    private int code;

    @Field
    @NotNull
    @Column(allowsNull = "false")
    private Language language;

    public NationalDefaultLanguage(Language language) {
        this.language = language;
    }

    public int getCode() {
        return code;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }
}
