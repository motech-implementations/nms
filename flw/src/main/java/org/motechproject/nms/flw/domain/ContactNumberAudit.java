package org.motechproject.nms.flw.domain;

import org.joda.time.LocalDate;
import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;

@Entity(tableName = "nms_contactNumber_audit")
public class ContactNumberAudit {

    @Field
    private LocalDate importDate;

    @Field
    private Long flwId;

    @Field
    private Long oldCallingNumber;

    @Field
    private Long newCallingNumber;

    public ContactNumberAudit(Long flwId) {
        this(null, flwId, null, null);
    }

    public ContactNumberAudit(LocalDate importDate, Long flwId, Long oldCallingNumber, Long newCallingNumber) {
        this.importDate = importDate;
        this.flwId = flwId;
        this.oldCallingNumber = oldCallingNumber;
        this.newCallingNumber = newCallingNumber;
    }

    public LocalDate getImportDate() {
        return importDate;
    }

    public void setImportDate(LocalDate importDate) {
        this.importDate = importDate;
    }


    public Long getOldCallingNumber() {
        return oldCallingNumber;
    }

    public void setOldCallingNumber(Long oldCallingNumber) {
        this.oldCallingNumber = oldCallingNumber;
    }

    public Long getFlwId() {
        return flwId;
    }

    public void setFlwId(Long flwId) {
        this.flwId = flwId;
    }

    public Long getNewCallingNumber() {
        return newCallingNumber;
    }

    public void setNewCallingNumber(Long newCallingNumber) {
        this.newCallingNumber = newCallingNumber;
    }

}
