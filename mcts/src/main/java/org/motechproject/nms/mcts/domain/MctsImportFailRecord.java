package org.motechproject.nms.mcts.domain;


import org.joda.time.LocalDate;
import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;

/**
 * Record for Mcts Import Failures
 */
@Entity(tableName = "nms_mcts_failures")
public class MctsImportFailRecord {

    @Field
    private LocalDate importDate;

    @Field
    private MctsUserType userType;

    @Field
    private Long stateCode;

    public MctsImportFailRecord(LocalDate importDate, MctsUserType userType, Long stateCode) {
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
}