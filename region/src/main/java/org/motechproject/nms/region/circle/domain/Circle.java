package org.motechproject.nms.region.circle.domain;

import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;
import org.motechproject.nms.region.location.domain.State;

import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.Unique;
import java.util.ArrayList;
import java.util.List;

@Entity(tableName = "nms_circles", recordHistory = true)
public class Circle {
    @Field
    @Unique
    private String name;

    @Field
    @Persistent(mappedBy = "circles", defaultFetchGroup = "true")
    private List<State> states;

    public Circle() {
        this.states = new ArrayList<>();
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
