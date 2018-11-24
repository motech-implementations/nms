package org.motechproject.nms.imi.web;

import org.apache.commons.lang3.StringUtils;
import org.motechproject.alerts.contract.AlertService;
import org.motechproject.alerts.domain.AlertStatus;
import org.motechproject.alerts.domain.AlertType;
import org.motechproject.nms.imi.domain.FileAuditRecord;
import org.motechproject.nms.imi.domain.FileType;
import org.motechproject.nms.imi.exception.ExecException;
import org.motechproject.nms.imi.exception.InvalidCallRecordFileException;
import org.motechproject.nms.imi.exception.NotFoundException;
import org.motechproject.nms.imi.repository.FileAuditRecordDataService;
import org.motechproject.nms.imi.service.CdrFileService;
import org.motechproject.nms.imi.service.TargetFileService;
import org.motechproject.nms.imi.service.contract.TargetFileNotification;
import org.motechproject.nms.imi.service.impl.ScpHelper;
import org.motechproject.nms.imi.web.contract.AggregateBadRequest;
import org.motechproject.nms.imi.web.contract.BadRequest;
import org.motechproject.nms.imi.web.contract.CdrFileNotificationRequest;
import org.motechproject.nms.imi.web.contract.FileInfo;
import org.motechproject.nms.imi.web.contract.FileProcessedStatusRequest;
import org.motechproject.server.config.SettingsFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import javax.servlet.http.HttpServletRequest;
import java.util.regex.Pattern;

/**
 * IMI Controller - handles all API interaction with the IMI IVR vendor
 */
@Controller
public class ImiController {

    public static final String NOT_PRESENT = "<%s: Not Present>";
    public static final String INVALID = "<%s: Invalid>";
    public static final String INVALID_STATUS_ENUM = "Can not construct instance of " +
            "org.motechproject.nms.imi.domain.FileProcessedStatus from String value";
    public static final Pattern TARGET_FILENAME_PATTERN = Pattern.compile("OBD_NMS_[0-9]{14}\\.csv");
    public static final String IVR_INTERACTION_LOG = "IVR INTERACTION: %s";

    private static final Logger LOGGER = LoggerFactory.getLogger(ImiController.class);
    public static final String LOG_RESPONSE_FORMAT = "RESPONSE: HTTP %d - %s";

    private SettingsFacade settingsFacade;
    private CdrFileService cdrFileService;
    private TargetFileService targetFileService;
    private FileAuditRecordDataService fileAuditRecordDataService;
    private AlertService alertService;


    @Autowired
    public ImiController(SettingsFacade settingsFacade, CdrFileService cdrFileService, AlertService alertService,
                         TargetFileService targetFileService, FileAuditRecordDataService fileAuditRecordDataService) {
        this.settingsFacade = settingsFacade;
        this.cdrFileService = cdrFileService;
        this.alertService = alertService;
        this.targetFileService = targetFileService;
        this.fileAuditRecordDataService = fileAuditRecordDataService;
    }


    protected static void log(final String endpoint, final String s) {
        LOGGER.info(IVR_INTERACTION_LOG.format(endpoint) + (StringUtils.isBlank(s) ? "" : " : " + s));
    }


    private static boolean validateFieldPresent(StringBuilder errors, String fieldName, Object value) {
        if (value != null) {
            return true;
        }
        errors.append(String.format(NOT_PRESENT, fieldName));
        return false;
    }


    // verify the passed targetFileName is valid
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


    // verify the detail/summary file info is valid
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


    private void verifyFileExistsInAuditRecord(String fileName) {
        if (fileAuditRecordDataService.countFindByFileName(fileName) < 1) {
            throw new NotFoundException(String.format("<%s: Not found in FileAuditRecord>", fileName));
        }
    }


    /**
     * "manually" trigger the target file generation - used for ITs only
     * *** NOTE: the scheduler will also trigger the file generation so some unexpected side effect is very likely
     */
    @RequestMapping("/generateTargetFile")
    @ResponseBody
    public String generateTargetFile() {

        LOGGER.debug("/generateTargetFile (GET)");
        TargetFileNotification tfn = targetFileService.generateTargetFile();
        LOGGER.debug("targetFileService.generateTargetFile() done");

        return tfn == null ? "null" : tfn.getFileName();
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

        log("REQUEST: /cdrFileNotification (POST)", request.toString());

        StringBuilder failureReasons = new StringBuilder();

        // Sanity check on the arguments passed in the json structure
        validateTargetFileName(failureReasons, request.getFileName());
        validateCdrFileInfo(failureReasons, request.getCdrSummary(), "cdrSummary", request.getFileName());
        validateCdrFileInfo(failureReasons, request.getCdrDetail(), "cdrDetail", request.getFileName());

        if (failureReasons.length() > 0) {
            throw new IllegalArgumentException(failureReasons.toString());
        }


        // Check the provided OBD file (aka: targetFile) exists in the FileAuditRecord table
        verifyFileExistsInAuditRecord(request.getFileName());


        // Copy the detail file from the IMI share (imi.remote_cdr_dir) into local cdr dir (imi.local_cdr_dir)
        ScpHelper scpHelper = new ScpHelper(settingsFacade);
        String fileName = request.getCdrDetail().getCdrFile();
        try {
            scpHelper.scpCdrFromRemote(fileName);
        } catch (ExecException e) {
            String error = String.format("Error copying CDR file %s: %s", fileName, e.getMessage());
            LOGGER.error(error);
            fileAuditRecordDataService.create(new FileAuditRecord(
                    FileType.CDR_DETAIL_FILE,
                    fileName,
                    false,
                    error,
                    null,
                    null
            ));
            alertService.create(fileName, "scpCdrFromRemote", error, AlertType.CRITICAL, AlertStatus.NEW, 0, null);
            throw new IllegalArgumentException(e.getMessage(), e);
        }

        // Copy the summary file from the IMI share (imi.remote_cdr_dir) into local cdr dir (imi.local_cdr_dir)
        fileName = request.getCdrSummary().getCdrFile();
        try {
            scpHelper.scpCdrFromRemote(fileName);
        } catch (ExecException e) {
            String error = String.format("Error copying CSR file %s: %s", fileName, e.getMessage());
            LOGGER.error(error);
            fileAuditRecordDataService.create(new FileAuditRecord(
                    FileType.CDR_SUMMARY_FILE,
                    fileName,
                    false,
                    error,
                    null,
                    null
            ));
            alertService.create(fileName, "scpCdrFromRemote", error, AlertType.CRITICAL, AlertStatus.NEW, 0, null);
            throw new IllegalArgumentException(e.getMessage(), e);
        }

        // Checks the CSR/CDR checksum, record count & csv, then sends an event to proceed to phase 2 of the
        // CDR processing task also handled by the IMI module: cdrProcessPhase234
        cdrFileService.cdrProcessPhase1(request);

        LOGGER.debug("RESPONSE: HTTP {}", HttpStatus.ACCEPTED);
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

        log("REQUEST: /obdFileProcessedStatusNotification (POST)", request.toString());

        StringBuilder failureReasons = new StringBuilder();

        validateFieldPresent(failureReasons, "fileProcessedStatus",
                request.getFileProcessedStatus());
        validateFieldPresent(failureReasons, "fileName", request.getFileName());

        if (failureReasons.length() > 0) {
            throw new IllegalArgumentException(failureReasons.toString());
        }

        // Check the provided OBD file (aka: targetFile) exists in the FileAuditRecord table
        verifyFileExistsInAuditRecord(request.getFileName());

        //
        targetFileService.handleFileProcessedStatusNotification(request);

        LOGGER.debug("RESPONSE: {}", HttpStatus.OK);
    }


    @ExceptionHandler({ NotFoundException.class })
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public BadRequest handleException(NotFoundException e, HttpServletRequest request) {
        log(String.format(LOG_RESPONSE_FORMAT, HttpStatus.NOT_FOUND.value(), request.getRequestURI()), e.getMessage());
        return new BadRequest(e.getMessage());
    }


    @ExceptionHandler({ RuntimeException.class })
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public BadRequest handleException(RuntimeException e, HttpServletRequest request) {
        log(String.format(LOG_RESPONSE_FORMAT, HttpStatus.INTERNAL_SERVER_ERROR.value(), request.getRequestURI()), e.getMessage());
        return new BadRequest(e.toString());
    }

    @ExceptionHandler({ IllegalArgumentException.class })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public BadRequest handleException(IllegalArgumentException e, HttpServletRequest request) {
        log(String.format(LOG_RESPONSE_FORMAT, HttpStatus.BAD_REQUEST.value(), request.getRequestURI()), e.getMessage());
        return new BadRequest(e.getMessage());
    }


    /**
     * Handles malformed JSON, returns a slightly more informative message than a generic HTTP-400 Bad Request
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public BadRequest handleException(HttpMessageNotReadableException e, HttpServletRequest request) {
        log(String.format(LOG_RESPONSE_FORMAT, HttpStatus.BAD_REQUEST.value(), request.getRequestURI()), e.getMessage());
        if (e.getLocalizedMessage().contains(INVALID_STATUS_ENUM)) {
            return new BadRequest("<fileProcessedStatus: Invalid Value>");
        }
        return new BadRequest(e.getMessage());
    }




    /**
     * Handles InvalidCallRecordFileException - potentially a large amount of errors all in one list of string
     */
    //todo: IT or UT
    @ExceptionHandler(InvalidCallRecordFileException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public AggregateBadRequest handleException(InvalidCallRecordFileException e, HttpServletRequest request) {
        log(String.format(LOG_RESPONSE_FORMAT, HttpStatus.BAD_REQUEST.value(), request.getRequestURI()), e.getMessages().toString());
        return new AggregateBadRequest(e.getMessages());
    }


    /**
     * Handles any other exception
     */
    @ExceptionHandler(Exception.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public BadRequest handleException(Exception e, HttpServletRequest request) {
        log(String.format(LOG_RESPONSE_FORMAT, HttpStatus.INTERNAL_SERVER_ERROR.value(), request.getRequestURI()), e.getMessage());
        return new BadRequest(e.getMessage());
    }
}
