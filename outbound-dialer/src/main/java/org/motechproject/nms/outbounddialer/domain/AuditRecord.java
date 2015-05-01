package org.motechproject.nms.outbounddialer.domain;

import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;
import org.motechproject.mds.annotations.UIDisplayable;

@Entity(tableName = "nms_obd_audit_records")
public class AuditRecord {
    @Field
    @UIDisplayable(position = 0)
    private FileType type;

    @Field
    @UIDisplayable(position = 1)
    private String file;

    @Field
    @UIDisplayable(position = 3)
    private Integer recordCount;

    @Field
    @UIDisplayable(position = 4)
    private String checksum;

    @Field
    @UIDisplayable(position = 2)
    private String status;

    public AuditRecord() { }

    public AuditRecord(FileType type, String file, Integer recordCount, String checksum, String status) {
        this.type = type;
        this.file = file;
        this.recordCount = recordCount;
        this.checksum = checksum;
        this.status = status;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "AuditRecord{" +
                "type=" + type +
                ", file='" + file + '\'' +
                ", recordCount=" + recordCount +
                ", checksum='" + checksum + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
