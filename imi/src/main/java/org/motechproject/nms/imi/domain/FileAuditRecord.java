package org.motechproject.nms.imi.domain;

import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;

import javax.jdo.annotations.Column;
import javax.validation.constraints.Min;

@Entity(tableName = "nms_imi_file_audit_records")
public class FileAuditRecord {
    @Field
    @Column(allowsNull = "false")
    private FileType type;

    @Field
    @Column(allowsNull = "false")
    private String fileName;

    @Field
    @Column(allowsNull = "false")
    private Boolean success;

    @Field
    private String error;

    @Field
    @Min(0)
    private Integer recordCount;

    @Field
    @Column(length = 32)
    private String checksum;

    public FileAuditRecord() { }

    public FileAuditRecord(FileType type, String fileName, Boolean success, String error, Integer recordCount,
                           String checksum) {
        this.type = type;
        this.fileName = fileName;
        this.success = success;
        this.error = error;
        this.recordCount = recordCount;
        this.checksum = checksum;
    }

    public FileType getType() {
        return type;
    }

    public void setType(FileType type) {
        this.type = type;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Integer getRecordCount() {
        return recordCount;
    }

    public void setRecordCount(Integer recordCount) {
        this.recordCount = recordCount;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    @Override
    public String toString() {
        return "FileAuditRecord{" +
                "type=" + type +
                ", fileName='" + fileName + '\'' +
                ", success=" + success +
                ", error='" + error + '\'' +
                ", recordCount=" + recordCount +
                ", checksum='" + checksum + '\'' +
                '}';
    }
}
