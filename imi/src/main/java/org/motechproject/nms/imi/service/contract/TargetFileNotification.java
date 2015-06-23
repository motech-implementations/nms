package org.motechproject.nms.imi.service.contract;

/**
 * Request body
 *
 * 4.3.1 TargetFile Notification API
 * NMS shall invoke this API to notify IVR platform when a target file is ready.
 * http://<IVROBDAPI:port>/obdmanager/notifytargetfile
 */
public class TargetFileNotification {
    private String fileName;
    private String checksum;
    private Integer recordsCount;

    public TargetFileNotification() { }

    public TargetFileNotification(String fileName, String checksum, Integer recordsCount) {
        this.fileName = fileName;
        this.checksum = checksum;
        this.recordsCount = recordsCount;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public Integer getRecordsCount() {
        return recordsCount;
    }

    public void setRecordsCount(Integer recordsCount) {
        this.recordsCount = recordsCount;
    }

    @Override
    public String toString() {
        return "TargetFileNotification{" +
                "fileName='" + fileName + '\'' +
                ", checksum='" + checksum + '\'' +
                ", recordsCount=" + recordsCount +
                '}';
    }
}
