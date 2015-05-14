package org.motechproject.nms.imi.web;

import org.motechproject.nms.imi.domain.FileAuditRecord;
import org.motechproject.nms.imi.exception.NotFoundException;
import org.motechproject.nms.imi.repository.FileAuditRecordDataService;
import org.motechproject.nms.imi.service.CdrFileService;
import org.motechproject.nms.imi.service.TargetFileService;
import org.motechproject.nms.imi.web.contract.BadRequest;
import org.motechproject.nms.imi.web.contract.CdrFileNotificationRequest;
import org.motechproject.nms.imi.web.contract.FileInfo;
import org.motechproject.nms.imi.web.contract.FileProcessedStatusRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;
import java.util.regex.Pattern;

/**
 * IMI Controller - handles all API interaction with the IMI IVR vendor
 */
@Controller
public class ImiController {

    public static final String NOT_PRESENT = "<%s: Not Present>";
    public static final String INVALID = "<%s: Invalid>";
    //todo: should we verify this is a valid yyyymmddhhmmss?
    public static final Pattern TARGET_FILENAME_PATTERN = Pattern.compile("OBD_[0-9]{14}\\.csv");

    private CdrFileService cdrFileService;
    private TargetFileService targetFileService;
    private FileAuditRecordDataService fileAuditRecordDataService;



    @Autowired
    public ImiController(CdrFileService cdrFileService, TargetFileService targetFileService,
                         FileAuditRecordDataService fileAuditRecordDataService) {
        this.cdrFileService = cdrFileService;
        this.targetFileService = targetFileService;
        this.fileAuditRecordDataService = fileAuditRecordDataService;
    }



    private static boolean validateFieldPresent(StringBuilder errors, String fieldName, Object value) {
        if (value != null) {
            return true;
        }
        errors.append(String.format(NOT_PRESENT, fieldName));
        return false;
    }



    private static boolean validateTargetFileName(StringBuilder errors, String targetFileName) {
        if (validateFieldPresent(errors, "fileName", targetFileName)) {
            if (TARGET_FILENAME_PATTERN.matcher(targetFileName).matches()) {
                return true;
            } else {
                errors.append(String.format(INVALID, "fileName"));
                return false;
            }
        }
        return false;
    }


    private static boolean validateCdrFileInfo(StringBuilder errors, FileInfo fileInfo,
        String fieldName, String targetFileName) {

        boolean valid = true;

        if (fileInfo == null) {
            errors.append(String.format(NOT_PRESENT, fieldName));
            return false;
        }

        if (validateFieldPresent(errors, "cdrFile", fileInfo.getCdrFile())) {
            if (!fileInfo.getCdrFile().equals(fieldName + "_" + targetFileName)) {
                errors.append(String.format(INVALID, fieldName));
                valid = false;
            }
        }
        
        if (!validateFieldPresent(errors, "checksum", fileInfo.getChecksum())) {
            valid = false;
        }

        if (fileInfo.getRecordsCount() < 0) {
            errors.append(String.format(INVALID, "recordsCount"));
            valid = false;
        }

        return valid;
    }



    /**
     * 4.2.6
     * CDR File Notification
     *
     * IVR shall invoke this API to notify when a CDR file is ready.
     */
    @RequestMapping(value = "/cdrFileNotification",
            method = RequestMethod.POST,
            headers = { "Content-type=application/json" })
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void notifyNewCdrFile(@RequestBody CdrFileNotificationRequest request) {
        StringBuilder failureReasons = new StringBuilder();
        validateTargetFileName(failureReasons, request.getFileName());
        validateCdrFileInfo(failureReasons, request.getCdrSummary(), "cdrSummary",
                request.getFileName());
        validateCdrFileInfo(failureReasons, request.getCdrDetail(), "cdrDetail",
                request.getFileName());

        if (failureReasons.length() > 0) {
            throw new IllegalArgumentException(failureReasons.toString());
        }

        cdrFileService.processCdrFile(request);
    }



    /**
     * 4.2.7
     * Notify File Processed Status
     *
     * IVR shall invoke this API to update about the status of file copy after initial checks on the file
     * are completed.
     */
    @RequestMapping(value = "obdFileProcessedStatusNotification",
            method = RequestMethod.POST,
            headers = { "Content-type=application/json" })
    @ResponseStatus(HttpStatus.OK)
    public void notifyFileProcessedStatus(@RequestBody FileProcessedStatusRequest request) {
        StringBuilder failureReasons = new StringBuilder();

        validateFieldPresent(failureReasons, "fileProcessedStatus",
                request.getFileProcessedStatus());
        validateFieldPresent(failureReasons, "fileName", request.getFileName());

        if (failureReasons.length() > 0) {
            throw new IllegalArgumentException(failureReasons.toString());
        }

        List<FileAuditRecord> records =  fileAuditRecordDataService.findByFileName(request.getFileName());
        if (records.size() < 1) {
            throw new NotFoundException("<fileName: Not Found>");
        }

        // call OBD service, which will handle notification
        targetFileService.handleFileProcessedStatusNotification(request);
    }



    @ExceptionHandler({ NotFoundException.class })
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public BadRequest handleException(NotFoundException e) {
        return new BadRequest(e.getMessage());
    }



    @ExceptionHandler({ IllegalArgumentException.class, IllegalStateException.class })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public BadRequest handleException(IllegalArgumentException e) {
        return new BadRequest(e.getMessage());
    }



    /**
     * Handles malformed JSON, returns a slightly more informative message than a generic HTTP-400 Bad Request
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public BadRequest handleException(HttpMessageNotReadableException e) {
        return new BadRequest(e.getMessage());
    }


}
