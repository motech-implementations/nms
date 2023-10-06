package org.motechproject.nms.rch.domain;

import org.joda.time.LocalDate;
import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;

/**
 * Created by vishnu on 21/7/17.
 */
@Entity(tableName = "nms_rch_import_facilitator")
public class RchImportFacilitator {

    @Field
    private String fileName;

    @Field
    private LocalDate startDate;

    @Field
    private LocalDate endDate;

    @Field
    private Long stateId;


    @Field
    private RchUserType userType;

    @Field
    private LocalDate importDate;

    @Field
    private Boolean finalStatus;

    public RchImportFacilitator(String fileName, LocalDate startDate, LocalDate endDate, Long stateId, RchUserType userType, LocalDate importDate, Boolean finalStatus) {
        this.fileName = fileName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.stateId = stateId;
        this.userType = userType;
        this.importDate = importDate;
        this.finalStatus = finalStatus;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Long getStateId() {
        return stateId;
    }

    public void setStateId(Long stateId) {
        this.stateId = stateId;
    }

    public Boolean getFinalStatus() { return finalStatus; }

    public void setFinalStatus(Boolean finalStatus) { this.finalStatus = finalStatus; }

    public RchUserType getUserType() {
        return userType;
    }

    public void setUserType(RchUserType userType) {
        this.userType = userType;
    }

    public LocalDate getImportDate() {
        return importDate;
    }

    public void setImportDate(LocalDate importDate) {
        this.importDate = importDate;
    }
}
