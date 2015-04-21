package org.motechproject.nms.flw.domain;

import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;

import org.motechproject.nms.language.domain.Language;
import org.motechproject.nms.location.domain.District;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.Unique;

@Entity
public class FrontLineWorker {

    @Field
    private Long id;

    @Field(required = true)
    @Unique
    @Column(length = 10)
    private String contactNumber;

    @Field
    private String name;

    @Field
    private Language language;

    @Field
    private District district;

    public FrontLineWorker(String name, String contactNumber) {
        this.name = name;
        this.contactNumber = contactNumber;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    public District getDistrict() {
        return district;
    }

    public void setDistrict(District district) {
        this.district = district;
    }

    @Override //NO CHECKSTYLE CyclomaticComplexity
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        FrontLineWorker that = (FrontLineWorker) o;

        if (!id.equals(that.id)) {
            return false;
        }
        if (!contactNumber.equals(that.contactNumber)) {
            return false;
        }
        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }
        if (language != null ? !language.equals(that.language) : that.language != null) {
            return false;
        }
        return !(district != null ? !district.equals(that.district) : that.district != null);

    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + contactNumber.hashCode();
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (language != null ? language.hashCode() : 0);
        result = 31 * result + (district != null ? district.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "FrontLineWorker{" +
                "id=" + id +
                ", contactNumber='" + contactNumber + '\'' +
                ", name='" + name + '\'' +
                ", language=" + language +
                ", district=" + district +
                '}';
    }
}
