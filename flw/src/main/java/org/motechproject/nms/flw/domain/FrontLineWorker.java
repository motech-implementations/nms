package org.motechproject.nms.flw.domain;

import org.joda.time.DateTime;
import org.motechproject.mds.annotations.Cascade;
import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;
import org.motechproject.mds.domain.MdsEntity;
import org.motechproject.nms.region.domain.District;
import org.motechproject.nms.region.domain.Language;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.Index;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.Unique;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.ArrayList;
import java.util.List;

@Entity(tableName = "nms_front_line_workers")
@Index(name = "status_invalidationDate_composit_idx", members = { "status", "invalidationDate" })
public class FrontLineWorker extends MdsEntity {

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
    private DateTime invalidationDate;

    @Field
    private Language language;

    @Field
    @Persistent(defaultFetchGroup = "true")
    private District district;

    @Field
    @Cascade(delete = true)
    @Persistent(mappedBy = "frontLineWorker")
    private List<FrontLineWorkerLanguageChange> languageChanges;

    public FrontLineWorker(Long contactNumber) {
        this.contactNumber = contactNumber;
        this.languageChanges = new ArrayList<>();
    }

    public FrontLineWorker(String name, Long contactNumber) {
        this.name = name;
        this.contactNumber = contactNumber;
        this.languageChanges = new ArrayList<>();
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

        if (this.status == FrontLineWorkerStatus.INVALID) {
            setInvalidationDate(new DateTime());
        } else {
            setInvalidationDate(null);
        }
    }

    public DateTime getInvalidationDate() {
        return invalidationDate;
    }

    public void setInvalidationDate(DateTime invalidationDate) {
        this.invalidationDate = invalidationDate;
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

    public List<FrontLineWorkerLanguageChange> getLanguageChanges() {
        return languageChanges;
    }

    public void setLanguageChanges(List<FrontLineWorkerLanguageChange> languageChanges) {
        this.languageChanges = languageChanges;
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

        if (!this.getId().equals(that.getId())) {
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
        int result = (getId() != null ? getId().hashCode() : 0);
        result = 31 * result + contactNumber.hashCode();
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (language != null ? language.hashCode() : 0);
        result = 31 * result + (district != null ? district.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "FrontLineWorker{" +
                "id=" + getId() +
                ", language=" + language +
                ", district=" + district +
                '}';
    }
}
