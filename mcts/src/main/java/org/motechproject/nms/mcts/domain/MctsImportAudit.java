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
    private LocalDate importDate;

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

    public MctsImportAudit(LocalDate importDate, MctsUserType mctsUserType, Long stateCode, String stateName, int accepted, int rejected, String message) {
        this.importDate = importDate;
        this.userType = mctsUserType;
        this.stateCode = stateCode;
        this.stateName = stateName;
        this.accepted = accepted;
        this.rejected = rejected;
        this.message = message;
    }

    public LocalDate getImportDate() {
        return importDate;
    }

    public void setImportDate(LocalDate importDate) {
        this.importDate = importDate;
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
