package org.motechproject.nms.language.domain;

import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.Unique;
import java.util.Objects;

/**
 * Represents all the languages that a circle (kind of a cell telephony geographic area in India) may contain
 */
@Entity(tableName = "nms_circle_languages")
@Unique(name = "uniqueCircleLanguage", members = {"circle", "language" })
public class CircleLanguage {
    public static final int FIELD_LENGTH_255 = 255;

    @Field
    @Column(length = FIELD_LENGTH_255)
    private String circle;

    @Field
    private Language language;

    public CircleLanguage() {
    }

    public CircleLanguage(String name, Language language) {
        this.circle = name;
        this.language = language;
    }

    public String getCircle() {
        return circle;
    }

    public void setCircle(String circle) {
        this.circle = circle;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    @Override
    public int hashCode() {
        return Objects.hash(circle, language);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        final CircleLanguage other = (CircleLanguage) obj;

        return Objects.equals(this.circle, other.circle) && Objects.equals(this.language, other.language);
    }

    @Override
    public String toString() {
        return String.format("CircleLanguage{circle='%s', language='%s'}", circle, language);
    }
}
