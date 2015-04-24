package org.motechproject.nms.outbounddialer.web;

import org.motechproject.nms.outbounddialer.web.contract.CdrFileNotificationRequest;
import org.motechproject.nms.outbounddialer.web.contract.BadRequest;
import org.motechproject.nms.outbounddialer.service.OutboundDialerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * OutboundDialerController
 */
@RequestMapping("/obd")
@Controller
public class OutboundDialerController {

    @Autowired
    private OutboundDialerService outboundDialerService;

    public static final String NOT_PRESENT = "<%s: Not Present>";
    public static final String INVALID = "<%s: Invalid>";


    protected static boolean validateFieldPresent(StringBuilder errors, String fieldName, String value) {
        if (value != null) {
            return true;
        }
        errors.append(String.format(NOT_PRESENT, fieldName));
        return false;
    }


    /**
     * 4.2.6
     * CDR File Notification
     *
     * IVR shall invoke this API to notify when a CDR file is ready.
     */
    @RequestMapping(value = "/cdrFileNotification",
            method = RequestMethod.POST,
            headers = {"Content-type=application/json"})
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void notifyNewCdrFile(@RequestBody CdrFileNotificationRequest cdrFileNotificationRequest) {
        // TODO: validate params

        // TODO: call OBD service, which will post a message that says new CDR file is ready
        outboundDialerService.handleNewCdrFile();
    }


    /**
     * 4.2.7
     * Notify File Processed Status
     *
     * IVR shall invoke this API to update about the status of file copy after initial checks on the file
     * are completed.
     */
    @RequestMapping(value = "/obdFileProcessedStatusNotification",
            method = RequestMethod.POST,
            headers = {"Content-type=application/json"})
    @ResponseStatus(HttpStatus.OK)
    public void notifyFileProcessedStatus(@RequestBody FileProcessedStatusRequest fileProcessedStatusRequest) {
        // TODO: validate params

        // TODO: call OBD service, which will handle notification
        outboundDialerService.handleFileProcessedStatusNotification();
    }



    @ExceptionHandler(IllegalArgumentException.class)
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
