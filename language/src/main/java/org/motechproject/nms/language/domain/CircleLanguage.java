package org.motechproject.nms.language.domain;

import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;

import java.util.Objects;

/**
 * Models data for simple records in a portable manner.
 */
@Entity
public class CircleLanguage {

    @Field
    private String circle;

    @Field
    private String language;

    public CircleLanguage() {
    }


    public CircleLanguage(String name, String language) {
        this.circle = name;
        this.language = language;
    }

    public String getCircle() {
        return circle;
    }

    public void setCircle(String circle) {
        this.circle = circle;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
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
