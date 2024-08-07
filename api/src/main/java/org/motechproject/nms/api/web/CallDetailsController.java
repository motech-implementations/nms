package org.motechproject.nms.api.web;

import org.joda.time.DateTime;
import org.motechproject.nms.api.web.contract.CallContentRequest;
import org.motechproject.nms.api.web.contract.CallDetailRecordRequest;
import org.motechproject.nms.flw.domain.CallContent;
import org.motechproject.nms.flw.domain.CallDetailRecord;
import org.motechproject.nms.flw.domain.FlwStatusUpdateAudit;
import org.motechproject.nms.flw.domain.FrontLineWorker;
import org.motechproject.nms.flw.domain.FrontLineWorkerStatus;
import org.motechproject.nms.flw.domain.UpdateStatusType;
import org.motechproject.nms.flw.repository.FlwStatusUpdateAuditDataService;
import org.motechproject.nms.flw.service.CallContentService;
import org.motechproject.nms.flw.service.CallDetailRecordService;
import org.motechproject.nms.flw.service.FrontLineWorkerService;
import org.motechproject.nms.props.domain.FinalCallStatus;
import org.motechproject.nms.props.domain.Service;
import org.motechproject.nms.props.service.LogHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
public class CallDetailsController extends BaseController {

    public static final int MILLISECONDS_PER_SECOND = 1000;
    private static final String QUESTION_TYPE = "question";
    private static final String LESSON_TYPE = "lesson";
    private static final String CHAPTER_TYPE = "chapter";

    @Autowired
    private CallDetailRecordService callDetailRecordService;

    @Autowired
    private CallContentService callContentService;

    @Autowired
    private FrontLineWorkerService frontLineWorkerService;

    @Autowired
    private FlwStatusUpdateAuditDataService flwStatusUpdateAuditDataService;

    private static final Logger LOGGER = LoggerFactory.getLogger(CallDetailsController.class);
    /**
     * 2.2.6 Save CallDetails API
     * IVR shall invoke this API to send MA call details to MoTech.
     * /api/mobileacademy/callDetails
     *
     * 3.2.2 Save Call Details API
     * This API enables IVR to send call details to NMS_MoTech_MK. This data is further saved in NMS database and used
     *    for reporting purpose.
     * /api/mobilekunji/callDetails
     *
     */
    @RequestMapping(value = "/{serviceName}/callDetails", // NO CHECKSTYLE Cyclomatic Complexity
            method = RequestMethod.POST,
            headers = { "Content-type=application/json" })
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public void saveCallDetails(@PathVariable String serviceName,
                                @RequestBody CallDetailRecordRequest callDetailRecordRequest) {

        log(String.format("REQUEST: /%s/callDetails (POST)", serviceName), LogHelper.nullOrString(callDetailRecordRequest));
        // log(new Gson().toJson(callDetailRecordRequest.getContent()));

        Service service = null;
        StringBuilder failureReasons;

        if (!(MOBILE_ACADEMY.equals(serviceName) || MOBILE_KUNJI.equals(serviceName))) {
            throw new IllegalArgumentException(String.format(INVALID, "serviceName"));
        }

        failureReasons = validate(callDetailRecordRequest.getCallingNumber(),
                callDetailRecordRequest.getCallId(), callDetailRecordRequest.getOperator(),
                callDetailRecordRequest.getCircle());

        // Verify common elements
        // (callStartTime, callEndTime, callDurationInPulses, endOfUsagePromptCount, callStatus,
        // callDisconnectReason)
        failureReasons.append(validateCallDetailsCommonElements(callDetailRecordRequest));

        if (MOBILE_ACADEMY.equals(serviceName)) {
            service = Service.MOBILE_ACADEMY;

            // Verify MA elements
            failureReasons.append(validateCallDetailsMobileAcademyElements(callDetailRecordRequest));
        }

        if (MOBILE_KUNJI.equals(serviceName)) {
            service = Service.MOBILE_KUNJI;

            // Verify MK elements (welcomeMessagePromptFlag)
            failureReasons.append(validateCallDetailsMobileKunjiElements(callDetailRecordRequest));
        }

        for (CallContentRequest callContentRequest : callDetailRecordRequest.getContent()) {
            failureReasons.append(validateCallContentRequest(service, callContentRequest));
        }

        if (failureReasons.length() > 0) {
            throw new IllegalArgumentException(failureReasons.toString());
        }

        FrontLineWorker flw = frontLineWorkerService.getByContactNumber(callDetailRecordRequest.getCallingNumber());
        if (null == flw) {
            // If the flw doesn't exist it is possible they hung up before providing their language.  We
            // create an anonymous stub flw here
            flw = new FrontLineWorker(callDetailRecordRequest.getCallingNumber());
            flw.setStatus(FrontLineWorkerStatus.ANONYMOUS);
            frontLineWorkerService.add(flw);

            // reload so the record can be linked to later.
            flw = frontLineWorkerService.getByContactNumber(callDetailRecordRequest.getCallingNumber());
        }

        createCallDetailRecord(flw, callDetailRecordRequest, service);

        // if this is the FLW's first time calling the service, set her status to ACTIVE based on NMS.GEN.FLW.003
        if (flw.getStatus() == FrontLineWorkerStatus.INACTIVE &&
                validateFlwNameAndNumber(flw)) {
           // flw.setStatus(FrontLineWorkerStatus.ACTIVE);
           // frontLineWorkerService.updateStatusToActive(flw);
            FlwStatusUpdateAudit flwStatusUpdateAudit = new FlwStatusUpdateAudit(DateTime.now(), flw.getFlwId(), flw.getMctsFlwId(), flw.getContactNumber(), UpdateStatusType.INACTIVE_TO_ACTIVE);
            flwStatusUpdateAuditDataService.create(flwStatusUpdateAudit);
        }
    }

    private boolean validateFlwLocation(FrontLineWorker flw) {
        return flw.getState() != null && flw.getDistrict() != null;
    }

    private boolean validateFlwNameAndNumber(FrontLineWorker flw) {
        return flw.getName() != null && flw.getContactNumber() != null;
    }

    private void createCallDetailRecord(FrontLineWorker flw, CallDetailRecordRequest callDetailRecordRequest,
                                        Service service) {
        CallDetailRecord cdr = new CallDetailRecord();
        cdr.setService(service);
        cdr.setFrontLineWorker(flw);
        cdr.setCallingNumber(callDetailRecordRequest.getCallingNumber());
        cdr.setCallId(callDetailRecordRequest.getCallId());
        cdr.setOperator(callDetailRecordRequest.getOperator());
        cdr.setCircle(callDetailRecordRequest.getCircle());
        cdr.setCallStartTime(new DateTime(callDetailRecordRequest.getCallStartTime() * MILLISECONDS_PER_SECOND));
        cdr.setCallEndTime(new DateTime(callDetailRecordRequest.getCallEndTime() * MILLISECONDS_PER_SECOND));
        cdr.setCallDurationInPulses(callDetailRecordRequest.getCallDurationInPulses());
        cdr.setEndOfUsagePromptCounter(callDetailRecordRequest.getEndOfUsagePromptCounter());
        cdr.setFinalCallStatus(FinalCallStatus.fromInt(callDetailRecordRequest.getCallStatus()));
        cdr.setCallDisconnectReason(callDetailRecordRequest.getCallDisconnectReason());

        if (service == Service.MOBILE_KUNJI) {
            cdr.setWelcomePrompt(callDetailRecordRequest.getWelcomeMessagePromptFlag());
        }

        callDetailRecordService.add(cdr);

        for (CallContentRequest callContentRequest : callDetailRecordRequest.getContent()) {
            CallContent content = new CallContent();

            content.setContentName(callContentRequest.getContentName());
            content.setContentFile(callContentRequest.getContentFileName());
            content.setStartTime(new DateTime(callContentRequest.getStartTime() * MILLISECONDS_PER_SECOND));
            content.setEndTime(new DateTime(callContentRequest.getEndTime() * MILLISECONDS_PER_SECOND));

            if (service == Service.MOBILE_KUNJI) {
                content.setMobileKunjiCardCode(callContentRequest.getMkCardCode());
            }

            if (service == Service.MOBILE_ACADEMY) {
                content.setType(callContentRequest.getType());
                content.setCorrectAnswerEntered(callContentRequest.isCorrectAnswerEntered()); // this could be null, if not question
                content.setCompletionFlag(callContentRequest.getCompletionFlag());
            }

            content.setCallDetailRecord(cdr);

            callContentService.add(content);
        }
    }

    // (callStartTime, callEndTime, callDurationInPulses, endOfUsagePromptCount, callStatus, callDisconnectReason)
    private String validateCallDetailsCommonElements(CallDetailRecordRequest callDetailRecordRequest) {
        StringBuilder failureReasons = new StringBuilder();

        if (null == callDetailRecordRequest.getCallStartTime()) {
            failureReasons.append(String.format(NOT_PRESENT, "callStartTime"));
        }

        if (null == callDetailRecordRequest.getCallEndTime()) {
            failureReasons.append(String.format(NOT_PRESENT, "callEndTime"));
        }

        if (null == callDetailRecordRequest.getCallDurationInPulses()) {
            failureReasons.append(String.format(NOT_PRESENT, "callDurationInPulses"));
        }

        if (null == callDetailRecordRequest.getEndOfUsagePromptCounter()) {
            failureReasons.append(String.format(NOT_PRESENT, "endOfUsagePromptCount"));
        }

        if (null == callDetailRecordRequest.getCallStatus()) {
            failureReasons.append(String.format(NOT_PRESENT, "callStatus"));
        }

        if (null == callDetailRecordRequest.getCallDisconnectReason()) {
            failureReasons.append(String.format(NOT_PRESENT, "callDisconnectReason"));
        }

        return failureReasons.toString();
    }

    // welcomeMessagePromptFlag
    private String validateCallDetailsMobileKunjiElements(CallDetailRecordRequest callDetailRecordRequest) {
        StringBuilder failureReasons = new StringBuilder();

        if (callDetailRecordRequest.getWelcomeMessagePromptFlag() == null) {
            failureReasons.append(String.format(NOT_PRESENT, "welcomeMessagePromptFlag"));
        }

        return failureReasons.toString();
    }

    private String validateCallDetailsMobileAcademyElements(CallDetailRecordRequest callDetailRecordRequest) {
        StringBuilder failureReasons = new StringBuilder();

        // validate content type. No validation on correctAnswered because a disconnect during question
        // might be null
        for (CallContentRequest callContentRequest : callDetailRecordRequest.getContent()) {
            String contentType = callContentRequest.getType();
            if (contentType != null &&
                !contentType.equals(QUESTION_TYPE) &&
                !contentType.equals(CHAPTER_TYPE) &&
                !contentType.equals(LESSON_TYPE)) {
                    failureReasons.append(String.format(INVALID, "CallContent_type"));
            }
        }

        return failureReasons.toString();
    }

    private String validateCallContentRequest(Service service, CallContentRequest callContentRequest) { // NO CHECKSTYLE Cyclomatic Complexity
        StringBuilder failureReasons = new StringBuilder();

        // Common elements (contentName, contentFile, startTime, endTime)
        if (null == callContentRequest.getContentName()) {
            failureReasons.append(String.format(NOT_PRESENT, "contentName"));
        }

        if (null == callContentRequest.getContentFileName()) {
            failureReasons.append(String.format(NOT_PRESENT, "contentFile"));
        }

        if (null == callContentRequest.getStartTime()) {
            failureReasons.append(String.format(NOT_PRESENT, "startTime"));
        }

        if (null == callContentRequest.getEndTime()) {
            failureReasons.append(String.format(NOT_PRESENT, "endTime"));
        }

        // MK elements (mkCardCode)
        if (service == Service.MOBILE_KUNJI) {
            if (null == callContentRequest.getMkCardCode() || callContentRequest.getMkCardCode().isEmpty()) {
                failureReasons.append(String.format(NOT_PRESENT, "mkCardCode"));
            }
        }

        // MA elements (type, completionFlag)
        if (service == Service.MOBILE_ACADEMY) {
            if (null == callContentRequest.getType()) {
                failureReasons.append(String.format(NOT_PRESENT, "type"));
            }

            if (null == callContentRequest.getCompletionFlag()) {
                failureReasons.append(String.format(NOT_PRESENT, "completionFlag"));
            }
        }

        return failureReasons.toString();
    }
}
