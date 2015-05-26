package org.motechproject.nms.region.domain;

import org.motechproject.mds.annotations.Cascade;
import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;
import org.motechproject.mds.domain.MdsEntity;

import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.Unique;
import java.util.ArrayList;
import java.util.List;

@Entity(tableName = "nms_circles")
public class Circle extends MdsEntity {
    @Field
    @Unique
    private String name;

    @Field
    @Persistent(mappedBy = "circles", defaultFetchGroup = "true")
    private List<State> states;

    @Field
    @Cascade(delete = true)
    @Persistent(mappedBy = "circle", defaultFetchGroup = "true")
    private List<LanguageLocation> languageLocations;

    public Circle() {
        this.states = new ArrayList<>();
        this.languageLocations = new ArrayList<>();
    }

    public Circle(String name) {
        this.name = name;
        this.states = new ArrayList<>();
        this.languageLocations = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<State> getStates() {
        return states;
    }

    public void setStates(List<State> states) {
        this.states = states;
    }

    public List<LanguageLocation> getLanguageLocations() {
        return languageLocations;
    }

    public void setLanguageLocations(List<LanguageLocation> languageLocations) {
        this.languageLocations = languageLocations;
    }

    public LanguageLocation getDefaultLanguageLocation() {
        for (LanguageLocation languageLocation : getLanguageLocations()) {
            if (languageLocation.isDefaultForCircle()) {
                return languageLocation;
            }
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Circle circle = (Circle) o;

        return !(name != null ? !name.equals(circle.name) : circle.name != null);

    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Circle{" +
                "name='" + name + '\'' +
                '}';
    }
}
