package org.motechproject.nms.api.web;

import org.motechproject.nms.api.web.contract.UserLanguageRequest;
import org.motechproject.nms.api.web.exception.NotAuthorizedException;
import org.motechproject.nms.api.web.exception.NotDeployedException;
import org.motechproject.nms.api.web.exception.NotFoundException;
import org.motechproject.nms.flw.domain.FrontLineWorker;
import org.motechproject.nms.flw.service.FrontLineWorkerService;
import org.motechproject.nms.props.domain.Service;
import org.motechproject.nms.region.domain.Language;
import org.motechproject.nms.region.domain.State;
import org.motechproject.nms.region.service.LanguageService;
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
public class LanguageController extends BaseController {

    public static final String LANGUAGE_LOCATION_CODE = "languageLocationCode";
    public static final String SERVICE_NAME = "serviceName";

    @Autowired
    private FrontLineWorkerService frontLineWorkerService;

    @Autowired
    private LanguageService languageService;

    /**
     * 2.2.7 Set User Language Location Code API
     * IVR shall invoke this API to provide user languageLocation preference to MoTech.
     * /api/mobileacademy/languageLocationCode
     *
     * 3.2.3 Set User Language Location Code API
     * IVR shall invoke this API to set the language location code of the user in NMS database.
     * /api/mobilekunji/languageLocationCode
     *
     */
    @RequestMapping(value = "/{serviceName}/languageLocationCode", // NO CHECKSTYLE Cyclomatic Complexity
            method = RequestMethod.POST,
            headers = { "Content-type=application/json" })
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public void setUserLanguageLocationCode(@PathVariable String serviceName,
                                            @RequestBody UserLanguageRequest userLanguageRequest) {
        Long callingNumber = userLanguageRequest.getCallingNumber();
        Long callId = userLanguageRequest.getCallId();
        String languageLocationCode = userLanguageRequest.getLanguageLocationCode();

        StringBuilder failureReasons = validate(callingNumber, callId);
        validateFieldPresent(failureReasons, LANGUAGE_LOCATION_CODE, userLanguageRequest.getLanguageLocationCode());

        if (!(MOBILE_ACADEMY.equals(serviceName) || MOBILE_KUNJI.equals(serviceName))) {
            failureReasons.append(String.format(INVALID, SERVICE_NAME));
        }

        Service service = null;

        service = getServiceFromName(serviceName);

        if (failureReasons.length() > 0) {
            throw new IllegalArgumentException(failureReasons.toString());
        }

        FrontLineWorker flw = frontLineWorkerService.getByContactNumber(callingNumber);
        if (flw == null) {
            flw = new FrontLineWorker(callingNumber);
        }

        Language language = languageService.getForCode(languageLocationCode);
        if (null == language) {
            throw new NotFoundException(String.format(NOT_FOUND, LANGUAGE_LOCATION_CODE));
        }

        flw.setLanguage(language);

        State state =  getStateForFrontLineWorker(flw, null);

        if (!serviceDeployedInUserState(service, state)) {
            throw new NotDeployedException(String.format(NOT_DEPLOYED, service));
        }

        if (!frontLineWorkerAuthorizedForAccess(flw, state)) {
            throw new NotAuthorizedException(String.format(NOT_AUTHORIZED, CALLING_NUMBER));
        }



        // MOTECH-1667 added to get an upsert method included
        if (flw.getId() == null) {
            frontLineWorkerService.add(flw);
        } else {
            frontLineWorkerService.update(flw);
        }
    }

}
