package org.motechproject.nms.mcts.domain;

import org.joda.time.LocalDate;
import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;

/**
 * Audit record for mcts data import
 */
@Entity(tableName = "nms_mcts_audit")
public class MctsImportAudit {

    @Field
    private LocalDate startImportDate;

    @Field
    private LocalDate endImportDate;

    @Field
    private MctsUserType userType;

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

    public MctsImportAudit(LocalDate startImportDate, LocalDate endImportDate, MctsUserType userType, Long stateCode, String stateName, int accepted, int rejected, String message) {
        this.startImportDate = startImportDate;
        this.endImportDate = endImportDate;
        this.userType = userType;
        this.stateCode = stateCode;
        this.stateName = stateName;
        this.accepted = accepted;
        this.rejected = rejected;
        this.message = message;
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

    public MctsUserType getUserType() {
        return userType;
    }

    public void setUserType(MctsUserType userType) {
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
