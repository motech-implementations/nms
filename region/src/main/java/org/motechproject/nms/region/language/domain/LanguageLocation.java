package org.motechproject.nms.region.language.domain;

import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;
import org.motechproject.nms.region.circle.domain.Circle;
import org.motechproject.nms.region.language.domain.validation.ValidLanguageLocation;
import org.motechproject.nms.region.location.domain.District;
import org.motechproject.nms.region.location.domain.State;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.Unique;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents all the languages that a circle (kind of a cell telephony geographic area in India) may contain
 */
@ValidLanguageLocation
@Entity(tableName = "nms_circle_languages")
@Unique(name = "uniqueCircleLanguage", members = {"circle", "language" })
public class LanguageLocation {

    @Field
    @Unique
    @NotNull
    @Column(allowsNull = "false")
    private String code;

    @Field
    @NotNull
    @Column(allowsNull = "false")
    private Language language;

    @Field
    @NotNull
    @Column(allowsNull = "false")
    private Circle circle;

    @Field
    private State state;

    @Field
    private Set<District> districts;

    @Field
    @NotNull
    @Column(allowsNull = "false")
    private boolean isDefaultLanguage = false;


    public LanguageLocation() {
        this.districts = new HashSet<>();
    }

    public LanguageLocation(String code, Circle name, Language language) {
        this.code = code;
        this.circle = name;
        this.language = language;
        this.districts = new HashSet<>();
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    public Circle getCircle() {
        return circle;
    }

    public void setCircle(Circle circle) {
        this.circle = circle;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public Set<District> getDistricts() {
        return districts;
    }

    public void setDistricts(Set<District> districts) {
        this.districts = districts;
    }

    public boolean isDefaultLanguage() {
        return isDefaultLanguage;
    }

    public void setIsDefaultLanguage(boolean isDefaultLanguage) {
        this.isDefaultLanguage = isDefaultLanguage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        LanguageLocation that = (LanguageLocation) o;

        return !(code != null ? !code.equals(that.code) : that.code != null);

    }

    @Override
    public int hashCode() {
        return code != null ? code.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "LanguageLocation{" +
                "code='" + code + '\'' +
                ", language=" + language +
                ", circle=" + circle +
                ", isDefaultLanguage=" + isDefaultLanguage +
                '}';
    }
}
