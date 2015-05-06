package org.motechproject.nms.region.language.domain;

import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;

import javax.jdo.annotations.Unique;

/**
 * Represents... yep that's right. The languages that are supported by the system.
 */
@Entity(tableName = "nms_languages")
public class Language {

    @Field
    @Unique
    private String name;

    @Field
    @Unique
    private String code;

    public Language() {
    }

    public Language(String name, String code) {
        this.name = name;
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Language language = (Language) o;

        if (!name.equals(language.name)) {
            return false;
        }

        return code.equals(language.code);

    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + code.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Language{" +
                "name='" + name + '\'' +
                ", code='" + code + '\'' +
                '}';
    }
}
