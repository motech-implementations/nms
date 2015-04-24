package org.motechproject.nms.api.web;

import org.motechproject.nms.api.web.contract.LanguageRequest;
import org.motechproject.nms.api.web.exception.NotAuthorizedException;
import org.motechproject.nms.api.web.exception.NotFoundException;
import org.motechproject.nms.flw.domain.FrontLineWorker;
import org.motechproject.nms.flw.service.FrontLineWorkerService;
import org.motechproject.nms.language.domain.Language;
import org.motechproject.nms.language.service.LanguageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
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
     * 2.2.7.1  Set User Language Location Code
     *          http://<motech:port>/motech­platform­server/module/mobileacademy/languageLocationCode
     *
     * 3.2.3.1  Set User Language Location Code
     *          http://<motech:port>/motech­platform­server/module/mobilekunji/languageLocationCode
     *
     *          IVR shall invoke this API to provide user languageLocation preference to MOTECH.
     *
     */
    @RequestMapping(value = "/{serviceName}/languageLocationCode",
            method = RequestMethod.POST,
            headers = { "Content-type=application/json" })
    @ResponseStatus(HttpStatus.OK)
    public void setUserLanguageLocationCode(@PathVariable String serviceName,
                                            @RequestBody LanguageRequest languageRequest) {
        Long callingNumber = languageRequest.getCallingNumber();
        Long callId = languageRequest.getCallId();
        String languageLocationCode = languageRequest.getLanguageLocationCode();

        StringBuilder failureReasons = validate(callingNumber, callId);
        validateFieldPresent(failureReasons, LANGUAGE_LOCATION_CODE, languageRequest.getLanguageLocationCode());

        if (null == languageLocationCode) {
            failureReasons.append(String.format(NOT_PRESENT, LANGUAGE_LOCATION_CODE));
        }

        if (!(MOBILE_ACADEMY.equals(serviceName) || MOBILE_KUNJI.equals(serviceName))) {
            failureReasons.append(String.format(INVALID, SERVICE_NAME));
        }

        if (failureReasons.length() > 0) {
            throw new IllegalArgumentException(failureReasons.toString());
        }

        FrontLineWorker flw = frontLineWorkerService.getByContactNumber(callingNumber);
        if (flw == null) {
            flw = new FrontLineWorker(callingNumber);
        }

        Language language = languageService.getLanguageByCode(languageLocationCode);
        if (null == language) {
            throw new NotFoundException(String.format(NOT_FOUND, LANGUAGE_LOCATION_CODE));
        }

        flw.setLanguage(language);

        if (!frontLineWorkerAuthorizedForAccess(flw)) {
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
