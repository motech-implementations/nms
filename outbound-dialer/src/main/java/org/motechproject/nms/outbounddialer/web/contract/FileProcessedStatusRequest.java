package org.motechproject.nms.outbounddialer.web.contract;

import org.motechproject.nms.outbounddialer.domain.FileProcessedStatus;

/**
 * POST request data for 4.2.7
 * /outbound-dialer/obdFileProcessedStatusNotification
 *
 * IVR shall invoke the notification API of MOTECH to give update about the status of file copy after the initial
 * checks on the file are completed.
 */
public class FileProcessedStatusRequest {
    private FileProcessedStatus fileProcessedStatus;
    private String fileName;

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

    @Override
    public String toString() {
        return "FileProcessedStatusRequest{" +
                "fileProcessedStatus=" + fileProcessedStatus +
                ", fileName='" + fileName + '\'' +
                '}';
    }
}
