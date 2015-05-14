package org.motechproject.nms.flw.domain;

import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;
import org.motechproject.nms.region.domain.District;
import org.motechproject.nms.region.domain.LanguageLocation;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.Unique;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@Entity(tableName = "nms_front_line_workers")
public class FrontLineWorker {

    @Field
    private Long id;

    @Field
    private String flwId;

    @Field
    private String mctsFlwId;

    @Field(required = true)
    @Unique
    @Min(value = 1000000000L, message = "contactNumber must be 10 digits")
    @Max(value = 9999999999L, message = "contactNumber must be 10 digits")
    @Column(length = 10)
    private Long contactNumber;

    @Field
    private String name;

    @Field
    private FrontLineWorkerStatus status;

    @Field
    private LanguageLocation languageLocation;

    @Field
    @Persistent(defaultFetchGroup = "true")
    private District district;

    public FrontLineWorker(Long contactNumber) {
        this.contactNumber = contactNumber;
    }

    public FrontLineWorker(String name, Long contactNumber) {
        this.name = name;
        this.contactNumber = contactNumber;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFlwId() {
        return flwId;
    }

    public void setFlwId(String flwId) {
        this.flwId = flwId;
    }

    public String getMctsFlwId() {
        return mctsFlwId;
    }

    public void setMctsFlwId(String mctsFlwId) {
        this.mctsFlwId = mctsFlwId;
    }

    public Long getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(Long contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public FrontLineWorkerStatus getStatus() {
        return status;
    }

    public void setStatus(FrontLineWorkerStatus status) {
        this.status = status;
    }

    public LanguageLocation getLanguageLocation() {
        return languageLocation;
    }

    public void setLanguageLocation(LanguageLocation languageLocation) {
        this.languageLocation = languageLocation;
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
        if (languageLocation != null ? !languageLocation.equals(that.languageLocation) : that.languageLocation != null) {
            return false;
        }
        return !(district != null ? !district.equals(that.district) : that.district != null);

    }

    @Override
    public int hashCode() {
        int result = (id != null ? id.hashCode() : 0);
        result = 31 * result + contactNumber.hashCode();
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (languageLocation != null ? languageLocation.hashCode() : 0);
        result = 31 * result + (district != null ? district.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "FrontLineWorker{" +
                "id=" + id +
                ", contactNumber='" + contactNumber + '\'' +
                ", name='" + name + '\'' +
                ", languageLocation=" + languageLocation +
                ", district=" + district +
                '}';
    }
}
