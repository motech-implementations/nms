package org.motechproject.nms.imi.domain;

import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;

import javax.jdo.annotations.Column;

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
    private String status;

    @Field
    private Integer recordCount;

    @Field
    private String checksum;

    public FileAuditRecord() { }

    public FileAuditRecord(FileType type, String fileName, String status, Integer recordCount, String checksum) {
        this.type = type;
        this.fileName = fileName;
        this.status = status;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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
                ", type=" + type +
                ", fileName='" + fileName + '\'' +
                ", status='" + status + '\'' +
                ", recordCount=" + recordCount +
                ", checksum='" + checksum + '\'' +
                '}';
    }
}
