package org.motechproject.nms.rch.domain;

import org.joda.time.LocalDate;
import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;

/**
 * Audit record for rch data import
 */
@Entity(tableName = "nms_rch_audit")
public class RchImportAudit {
    /**
     * importDate is used in historic data.After fixing the issue NMS-360: Introduce retrial of data import from MCTS service for 7 days,
     * It is no longer used.
     */
    @Field
    private LocalDate importDate;

    @Field
    private LocalDate startImportDate;

    @Field
    private LocalDate endImportDate;

    @Field
    private RchUserType userType;

    @Field
    private Long stateCode;

    @Field
    private String stateName;

    @Field
    private int accepted;

    @Field
    private int rejected;

    @Field
    private String message;

    //CHECKSTYLE:OFF
    public RchImportAudit(LocalDate startImportDate, LocalDate endImportDate, RchUserType userType, Long stateCode, String stateName, int accepted, int rejected, String message) {
        this.startImportDate = startImportDate;
        this.endImportDate = endImportDate;
        this.userType = userType;
        this.stateCode = stateCode;
        this.stateName = stateName;
        this.accepted = accepted;
        this.rejected = rejected;
        this.message = message;
    }

    //CHECKSTYLE:ON
    public LocalDate getImportDate() {
        return importDate;
    }

    public void setImportDate(LocalDate importDate) {
        this.importDate = importDate;
    }
    public LocalDate getStartImportDate() {
        return startImportDate;
    }

    public void setStartImportDate(LocalDate startImportDate) {
        this.startImportDate = startImportDate;
    }

    public LocalDate getEndImportDate() {
        return endImportDate;
    }

    public void setEndImportDate(LocalDate endImportDate) {
        this.endImportDate = endImportDate;
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

    public String getStateName() {
        return stateName;
    }

    public void setStateName(String stateName) {
        this.stateName = stateName;
    }

    public int getAccepted() {
        return accepted;
    }

    public void setAccepted(int accepted) {
        this.accepted = accepted;
    }

    public int getRejected() {
        return rejected;
    }

    public void setRejected(int rejected) {
        this.rejected = rejected;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

