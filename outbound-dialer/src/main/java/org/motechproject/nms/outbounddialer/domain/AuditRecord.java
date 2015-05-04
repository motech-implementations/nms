package org.motechproject.nms.outbounddialer.domain;

import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;
import org.motechproject.mds.annotations.UIDisplayable;

import javax.jdo.annotations.Column;

@Entity(tableName = "nms_obd_audit_records")
public class AuditRecord {
    /**
     * The identifier field is used in each targetFile csv row combined with the subscription id to form a uniquely
     * identifying id for a specific OBD record
     */
    @Field
    @UIDisplayable(position = 0)
    private String identifier;


    @Field
    @UIDisplayable(position = 1)
    private FileType type;

    @Field
    @UIDisplayable(position = 2)
    private String file;

    @Field
    @UIDisplayable(position = 3)
    @Column(allowsNull = "false")
    private String status;

    @Field
    @UIDisplayable(position = 4)
    private Integer recordCount;

    @Field
    @UIDisplayable(position = 5)
    private String checksum;

    public AuditRecord() { }

    public AuditRecord(String identifier, FileType type, String file, String status, Integer recordCount,
                       String checksum) {
        this.identifier = identifier;
        this.type = type;
        this.file = file;
        this.status = status;
        this.recordCount = recordCount;
        this.checksum = checksum;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public FileType getType() {
        return type;
    }

    public void setType(FileType type) {
        this.type = type;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
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
        return "AuditRecord{" +
                "identifier='" + identifier + '\'' +
                ", type=" + type +
                ", file='" + file + '\'' +
                ", status='" + status + '\'' +
                ", recordCount=" + recordCount +
                ", checksum='" + checksum + '\'' +
                '}';
    }
}
