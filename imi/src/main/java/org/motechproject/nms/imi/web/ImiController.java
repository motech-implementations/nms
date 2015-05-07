package org.motechproject.nms.imi.web;

import org.motechproject.nms.imi.domain.AuditRecord;
import org.motechproject.nms.imi.domain.FileType;
import org.motechproject.nms.imi.repository.AuditDataService;
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

import java.util.regex.Pattern;

/**
 * IMI Controller - handles all API interaction with the IMI IVR vendor
 */
@Controller
public class ImiController {

    public static final String NOT_PRESENT = "<%s: Not Present>";
    public static final String INVALID = "<%s: Invalid>";
    public static final Pattern TARGET_FILENAME_PATTERN = Pattern.compile("OBD_NMS[1-9]_20[0-9]{12}\\.csv");

    private CdrFileService cdrFileService;
    private TargetFileService targetFileService;
    private AuditDataService auditDataService;



    @Autowired
    public ImiController(CdrFileService cdrFileService, TargetFileService targetFileService,
                         AuditDataService auditDataService) {
        this.cdrFileService = cdrFileService;
        this.targetFileService = targetFileService;
        this.auditDataService = auditDataService;
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
            auditDataService.create(new AuditRecord(null, FileType.CDR_FILE, request.getFileName(), null, null,
                    failureReasons.toString()));
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

        // TODO: validate file name against internal data

        if (failureReasons.length() > 0) {
            throw new IllegalArgumentException(failureReasons.toString());
        }

        // call OBD service, which will handle notification
        targetFileService.handleFileProcessedStatusNotification(request);
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
