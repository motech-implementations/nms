package org.motechproject.nms.region.domain;

import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;
import org.motechproject.mds.domain.MdsEntity;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.Unique;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents all the languages that a state or set of districtSet can have
 */
@Entity(tableName = "nms_language_locations", recordHistory = true)
public class LanguageLocation extends MdsEntity {

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

    // TODO: Rename to 'districts' once https://applab.atlassian.net/browse/MOTECH-1688 is resolved
    @Field
    @Size(min = 1)
    @Persistent(mappedBy = "languageLocation", defaultFetchGroup = "true")
    private Set<District> districtSet;

    public LanguageLocation() {
        this.districtSet = new HashSet<>();
    }

    public LanguageLocation(String code, Circle name, Language language) {
        this.code = code;
        this.circle = name;
        this.language = language;
        this.districtSet = new HashSet<>();
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
        // If this language location code covers a set of districtSet and they are all in the same state
        // return that state
        State ret = null;
        for (District district : districtSet) {
            if (ret == null) {
                ret = district.getState();
            }

            if (ret != null && ret != district.getState()) {
                return null;
            }
        }

        return ret;
    }

    public Set<District> getDistrictSet() {
        return districtSet;
    }

    public void setDistrictSet(Set<District> districtSet) {
        this.districtSet = districtSet;
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
                '}';
    }
}
