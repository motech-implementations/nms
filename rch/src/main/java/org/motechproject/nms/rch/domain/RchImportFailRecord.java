package org.motechproject.nms.rch.domain;

import org.joda.time.LocalDate;
import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;

/**
 * Record for RCH import failures
 */


@Entity(tableName = "nms_rch_failures")
public class RchImportFailRecord {

    @Field
    private LocalDate importDate;

    @Field
    private RchUserType userType;

    @Field
    private Long stateCode;

    public RchImportFailRecord(LocalDate importDate, RchUserType userType, Long stateCode) {
        this.importDate = importDate;
        this.userType = userType;
        this.stateCode = stateCode;
    }

    public LocalDate getImportDate() {
        return importDate;
    }

    public void setImportDate(LocalDate importDate) {
        this.importDate = importDate;
    }

    public RchUserType getUserType() {
        return userType;
    }

    public void setUserType(RchUserType userType) {
        this.userType = userType;
    }

    public Long getStateCode() {
        return stateCode;
    }

    public void setStateCode(Long stateCode) {
        this.stateCode = stateCode;
    }
}
