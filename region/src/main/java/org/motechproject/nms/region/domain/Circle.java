package org.motechproject.nms.region.domain;

import org.codehaus.jackson.annotate.JsonManagedReference;
import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;
import org.motechproject.mds.annotations.Ignore;
import org.motechproject.mds.annotations.InstanceLifecycleListeners;
import org.motechproject.mds.domain.MdsEntity;
import org.motechproject.nms.tracking.annotation.TrackClass;
import org.motechproject.nms.tracking.annotation.TrackFields;

import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.Unique;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity(tableName = "nms_circles")
@TrackClass
@TrackFields
@InstanceLifecycleListeners
public class Circle extends MdsEntity {
    @Field
    @Unique
    private String name;

    @Field
    @Persistent(mappedBy = "circle", defaultFetchGroup = "true")
    @JsonManagedReference
    private List<District> districtList;

    @Field
    private Language defaultLanguage;

    public Circle() {
    }

    public Circle(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<District> getDistrictList() {
        return districtList;
    }

    public void setDistrictList(List<District> districtList) {
        this.districtList = districtList;
    }

    public Language getDefaultLanguage() {
        return defaultLanguage;
    }

    public void setDefaultLanguage(Language defaultLanguage) {
        this.defaultLanguage = defaultLanguage;
    }

    @Ignore
    public Set<State> getStates() {
        Set<State> states = new HashSet<>();

        for (District district : this.getDistrictList()) {
            states.add(district.getState());
        }

        return states;
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
