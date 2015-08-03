package org.motechproject.nms.imi.web.contract;

import org.motechproject.nms.imi.domain.FileProcessedStatus;

/**
 * POST request data for 4.2.7
 * /imi/obdFileProcessedStatusNotification
 *
 * IVR shall invoke the notification API of MOTECH to give update about the status of file copy after the initial
 * checks on the file are completed.
 */
public class FileProcessedStatusRequest {
    private FileProcessedStatus fileProcessedStatus;
    private String fileName;
    private String failureReason;

    public FileProcessedStatusRequest() {
    }

    public FileProcessedStatusRequest(FileProcessedStatus fileProcessedStatus, String fileName) {
        this.fileProcessedStatus = fileProcessedStatus;
        this.fileName = fileName;
    }

    public FileProcessedStatus getFileProcessedStatus() { return fileProcessedStatus; }

    public void setFileProcessedStatus(FileProcessedStatus fileProcessedStatus) {
        this.fileProcessedStatus = fileProcessedStatus;
    }

    public String getFileName() { return fileName; }

    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    @Override
    public String toString() {
        return "FileProcessedStatusRequest{" +
                "fileProcessedStatus=" + fileProcessedStatus +
                ", fileName='" + fileName + '\'' +
                ", failureReason='" + failureReason + '\'' +
                '}';
    }
}
