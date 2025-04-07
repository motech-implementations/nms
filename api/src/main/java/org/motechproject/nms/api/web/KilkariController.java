package org.motechproject.nms.api.web;

import org.motechproject.nms.api.web.contract.kilkari.CallDataRequest;
import org.motechproject.nms.api.web.contract.kilkari.InboxCallDetailsRequest;
import org.motechproject.nms.api.web.contract.kilkari.InboxResponse;
import org.motechproject.nms.api.web.contract.kilkari.InboxSubscriptionDetailResponse;
import org.motechproject.nms.api.web.contract.kilkari.SubscriptionRequest;
import org.motechproject.nms.api.web.exception.NotDeployedException;
import org.motechproject.nms.api.web.exception.NotFoundException;
import org.motechproject.nms.kilkari.domain.*;
import org.motechproject.nms.kilkari.exception.NoInboxForSubscriptionException;
import org.motechproject.nms.kilkari.service.InboxService;
import org.motechproject.nms.kilkari.service.SubscriberService;
import org.motechproject.nms.kilkari.service.SubscriptionService;
import org.motechproject.nms.props.domain.Service;
import org.motechproject.nms.props.service.LogHelper;
import org.motechproject.nms.props.service.PropertyService;
import org.motechproject.nms.region.domain.Circle;
import org.motechproject.nms.region.domain.Language;
import org.motechproject.nms.region.domain.State;
import org.motechproject.nms.region.service.CircleService;
import org.motechproject.nms.region.service.LanguageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * KilkariController
 */
@RequestMapping("/kilkari")
@Controller
public class KilkariController extends BaseController {

    /*
     4.2.5.1.5 Body Elements
     */
    public static final int SUBSCRIPTION_ID_LENGTH = 36;
    private static final int DEFAULT_THREAD_COUNT = 8;
    public static final Set<String> SUBSCRIPTION_PACK_SET = new HashSet<>(Arrays.asList("48WeeksPack", "72WeeksPack"));

    @Autowired
    private SubscriberService subscriberService;

    @Autowired
    private SubscriptionService subscriptionService;

    @Autowired
    private LanguageService languageService;

    @Autowired
    private CircleService circleService;

    @Autowired
    private InboxService inboxService;

    @Autowired
    private PropertyService propertyService;

    private static final Logger LOGGER = LoggerFactory.getLogger(KilkariController.class);

    /**
     * 4.2.2 Get Inbox Details API
     * IVR shall invoke this API to get the Inbox details of the beneficiary identified by ‘callingNumber’.
     * /api/kilkari/inbox?callingNumber=1111111111&callId=123456789123456&languageLocationCode=10
     *
     */
    @Transactional(readOnly = true)
    @RequestMapping("/inbox")
    @ResponseBody
    public InboxResponse getInboxDetails(@RequestParam(required = false) Long callingNumber,
                                         @RequestParam(required = false) String callId) {

        log("REQUEST: /kilkari/inbox", String.format("callingNumber=%s, callId=%s",
                LogHelper.obscure(callingNumber), callId));

        StringBuilder failureReasons = validate(callingNumber, callId);
        if (failureReasons.length() > 0) {
            throw new IllegalArgumentException(failureReasons.toString());
        }

        List<Subscriber> subscribers = subscriberService.getSubscriber(callingNumber);
        if (subscribers.isEmpty()) {
            throw new NotFoundException(String.format(NOT_FOUND, "callingNumber"));
        }

        Set<InboxSubscriptionDetailResponse> subscriptionDetails = new HashSet<>();
        SubscriptionPackMessage inboxMessage;
        String weekId;
        String fileName;
        List<Subscription> holdToActiveSubscription = new ArrayList<>();
        boolean holdToActive = false;

        for (Subscriber subscriber : subscribers) {
            for (Subscription subscription : subscriber.getAllSubscriptions()) {
                try {
                    if (subscription.getStatus()== SubscriptionStatus.HOLD) {
                        holdToActive = true;
                        subscription.setStatus(SubscriptionStatus.ACTIVE);
                        holdToActiveSubscription.add(subscription);
                    }
                    inboxMessage = inboxService.getInboxMessage(subscription);
                    weekId = (inboxMessage == null) ? null : inboxMessage.getWeekId();
                    fileName = (inboxMessage == null) ? null : inboxMessage.getMessageFileName();

                    subscriptionDetails.add(new InboxSubscriptionDetailResponse(subscription.getSubscriptionId(),
                            subscription.getSubscriptionPack().getName(),
                            weekId,
                            fileName));

                } catch (NoInboxForSubscriptionException e) {
                    // there's no inbox, don't add anything to the list
                    LOGGER.debug(String.format("Found no inbox for subscription: %s", subscription.getSubscriptionId()));
                }
            }
        }
        if (holdToActive){
            subscriptionService.activateSubscription(holdToActiveSubscription.get(0));
        }

        InboxResponse ret = new InboxResponse(subscriptionDetails);
        log("RESPONSE: /kilkari/inbox", String.format("callId=%s, %s", callId, ret.toString()));
        return ret;
    }

    private void validateSaveInboxCallDetailsContent(StringBuilder failureReasons,
                                                     Set<CallDataRequest> content) {
        if (content == null || content.size() == 0) {
            // Empty content is acceptable (when the IVR vendor plays promotional content)
            return;
        }
        if (content.size() > 2) {
            // Valid content must contain max two elements
            failureReasons.append(String.format(INVALID, "content"));
            return;
        }
        int failureReasonsLength = failureReasons.length();
        Set<String> subscriptionPacks = new HashSet<>();
        for (CallDataRequest data : content) {
            if (data == null) {
                failureReasons.append(String.format(INVALID, "content"));
                continue;
            }
            validateFieldExactLength(failureReasons, "subscriptionId", data.getSubscriptionId(), SUBSCRIPTION_ID_LENGTH);
            subscriptionPacks.add(data.getSubscriptionPack());
            validateFieldString(failureReasons, "inboxWeekId", data.getInboxWeekId());
            validateFieldString(failureReasons, "contentFileName", data.getContentFileName());
            validateFieldPositiveLong(failureReasons, "startTime", data.getStartTime());
            validateFieldPositiveLong(failureReasons, "endTime", data.getEndTime());
        }

        //check we have all required subscription packs
        if (!SUBSCRIPTION_PACK_SET.containsAll(subscriptionPacks)) {
            failureReasons.append(String.format(INVALID, "subscriptionPack"));
        }

        //some field error occurred, add an error on the "content" parent field
        if (failureReasonsLength != failureReasons.length()) {
            failureReasons.append(String.format(INVALID, "content"));
        }

    }

    private StringBuilder validateSaveInboxCallDetails(InboxCallDetailsRequest request) {
        StringBuilder failureReasons = validate(request.getCallingNumber(), request.getCallId(),
                request.getOperator(), request.getCircle());

        validateFieldPresent(failureReasons, "callStartTime", request.getCallStartTime());
        validateFieldPresent(failureReasons, "callEndTime", request.getCallEndTime());
        validateFieldPresent(failureReasons, "callDurationInPulses", request.getCallDurationInPulses());
        validateFieldCallStatus(failureReasons, "callStatus", request.getCallStatus());
        validateFieldCallDisconnectReason(failureReasons, "callDisconnectReason", request.getCallDisconnectReason());
        validateSaveInboxCallDetailsContent(failureReasons, request.getContent());

        return failureReasons;
    }


    /**
     * 4.2.5 Save Inbox Call Details
     * IVR shall invoke this API to send the call detail information corresponding to the Inbox access inbound call for
     *    which inbox message(s) is played.
     * /api/kilkari/inboxCallDetails
     *
     */
    @RequestMapping(value = "/inboxCallDetails",
            method = RequestMethod.POST,
            headers = { "Content-type=application/json" })
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public void saveInboxCallDetails(@RequestBody InboxCallDetailsRequest request) {

        log("REQUEST: /kilkari/inboxCallDetails (POST)", LogHelper.nullOrString(request));

        StringBuilder failureReasons = validateSaveInboxCallDetails(request);
        if (failureReasons.length() > 0) {
            throw new IllegalArgumentException(failureReasons.toString());
        }

        Set<InboxCallData> content = new HashSet<>();
        if (request.getContent() != null && request.getContent().size() > 0) {
            for (CallDataRequest inboxCallDetailsRequestCallData : request.getContent()) {
                content.add(new InboxCallData(
                        inboxCallDetailsRequestCallData.getSubscriptionId(),
                        inboxCallDetailsRequestCallData.getSubscriptionPack(),
                        inboxCallDetailsRequestCallData.getInboxWeekId(),
                        inboxCallDetailsRequestCallData.getContentFileName(),
                        epochToDateTime(inboxCallDetailsRequestCallData.getStartTime()),
                        epochToDateTime(inboxCallDetailsRequestCallData.getEndTime())
                ));
            }
        }

        InboxCallDetailRecord inboxCallDetailRecord = new InboxCallDetailRecord(
                request.getCallingNumber(),
                request.getOperator(),
                request.getCircle(),
                request.getCallId(),
                epochToDateTime(request.getCallStartTime()),
                epochToDateTime(request.getCallEndTime()),
                request.getCallDurationInPulses(),
                request.getCallStatus(),
                request.getCallDisconnectReason(),
                content);

        inboxService.addInboxCallDetails(inboxCallDetailRecord);
    }

    /**
     * 4.2.3 Create Subscription Request API
     * IVR shall invoke this API to request the creation of the subscription of the beneficiary.
     * /api/kilkari/subscription
     */
    @RequestMapping(value = "/subscription",
            method = RequestMethod.POST,
            headers = { "Content-type=application/json" })
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public void createSubscription(@RequestBody SubscriptionRequest subscriptionRequest) {

        log("REQUEST: /kilkari/subscription (POST)", LogHelper.nullOrString(subscriptionRequest));

        StringBuilder failureReasons = validate(subscriptionRequest.getCallingNumber(),
                                                subscriptionRequest.getCallId(),
                                                subscriptionRequest.getOperator(),
                                                subscriptionRequest.getCircle());
        validateFieldPresent(failureReasons, "subscriptionPack", subscriptionRequest.getSubscriptionPack());
        validateFieldPresent(failureReasons, "languageLocationCode",
                             subscriptionRequest.getLanguageLocationCode());

        if (failureReasons.length() > 0) {
            throw new IllegalArgumentException(failureReasons.toString());
        }

        Language language;
        language = languageService.getForCode(subscriptionRequest.getLanguageLocationCode());
        if (language == null) {
            throw new NotFoundException(String.format(NOT_FOUND, "languageLocationCode"));
        }


        Circle circle = circleService.getByName(subscriptionRequest.getCircle());


        State state = getSingleStateFromCircleAndLanguage(circle, language);
        if (!propertyService.isServiceDeployedInState(Service.KILKARI, state)) {
            throw new NotDeployedException(String.format(NOT_DEPLOYED, Service.KILKARI));
        }

        SubscriptionPack subscriptionPack;
        subscriptionPack = subscriptionService.getSubscriptionPack(subscriptionRequest.getSubscriptionPack());
        if (subscriptionPack == null) {
            throw new NotFoundException(String.format(NOT_FOUND, "subscriptionPack"));
        }

        subscriptionService.createSubscription(null, subscriptionRequest.getCallingNumber(), language, circle,
                                               subscriptionPack, SubscriptionOrigin.IVR);
    }

    private State getSingleStateFromCircleAndLanguage(Circle circle, Language language) {

        Set<State> stateSet = languageService.getAllStatesForLanguage(language);

        if (stateSet.size() == 1) {
            return stateSet.iterator().next();
        }

        return getStateFromCircle(circle);
    }

    /**
     * 4.2.4
     * Deactivate Subscription
     * <p/>
     * IVR shall invoke this API to deactivate an existing Kilkari subscription
     */
    @RequestMapping(value = "/subscription",
            method = RequestMethod.DELETE,
            headers = { "Content-type=application/json" })
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public void deactivateSubscription(@RequestBody SubscriptionRequest subscriptionRequest) {

        log("REQUEST: /kilkari/subscription (DELETE)", LogHelper.nullOrString(subscriptionRequest));

        StringBuilder failureReasons = validate(subscriptionRequest.getCallingNumber(),
                subscriptionRequest.getCallId(), subscriptionRequest.getOperator(),
                subscriptionRequest.getCircle());

        validateFieldPresent(failureReasons, "subscriptionId", subscriptionRequest.getSubscriptionId());

        if (failureReasons.length() > 0) {
            throw new IllegalArgumentException(failureReasons.toString());
        }

        Subscription subscription = subscriptionService.getSubscription(subscriptionRequest.getSubscriptionId());

        if (subscription == null) {
            throw new NotFoundException(String.format(NOT_FOUND, "subscriptionId"));
        }

        subscriptionService.deactivateSubscription(subscription, DeactivationReason.DEACTIVATED_BY_USER);

        log("RESPONSE: /kilkari/subscription (DELETE)", "Success");
    }

    @RequestMapping(value = "/manualDeactivations", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Map<String, Object>> bulkDeactivation(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        if (file.isEmpty()) {
            response.put("error", "File is empty");
            return ResponseEntity.badRequest().body(response);
        }

        List<String> failureMessages = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger successCount = new AtomicInteger(0);

        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            List<String[]> records = readFileContent(br);

            if (records.isEmpty()) {
                response.put("error", "File contains no valid data.");
                return ResponseEntity.badRequest().body(response);
            }

            int threadCount = Math.min(DEFAULT_THREAD_COUNT, Runtime.getRuntime().availableProcessors());
            int chunkSize = (int) Math.ceil((double) records.size() / threadCount);

            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            List<Future<Void>> futures = new ArrayList<>();

            for (int i = 0; i < threadCount; i++) {
                int start = i * chunkSize;
                int end = Math.min(start + chunkSize, records.size());
                if (start >= records.size()) {break;}
                List<String[]> chunk = records.subList(start, end);

                Future<Void> future = executor.submit(new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        subscriberService.processChunkOfDeactivation(chunk, successCount, failureMessages);
                        return null;
                    }
                });
                futures.add(future);
            }

            for (Future<Void> future : futures) {
                future.get();
            }
            executor.shutdown();

        } catch (IOException e) {
            return handleException(response, "Failed to read the file", e);
        } catch (InterruptedException | ExecutionException e) {
            return handleException(response, "Error during processing", e);
        }
        response.put("successCount", successCount.get());
        response.put("failureMessages", failureMessages);
        return ResponseEntity.ok(response);
    }

    private List<String[]> readFileContent(BufferedReader reader) throws IOException {
        List<String[]> records = new ArrayList<>();
        String line;
        while ((line = reader.readLine()) != null) {
            if (!line.trim().isEmpty()) {
                records.add(line.split(","));
            }
        }
        return records;
    }

    private ResponseEntity<Map<String, Object>> handleException(Map<String, Object> response, String message, Exception e) {
        response.put("error", message);
        response.put("details", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

}
