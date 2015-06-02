package org.motechproject.nms.api.web.validator;

import org.motechproject.nms.api.web.contract.mobileAcademy.CourseResponse;
import org.motechproject.nms.api.web.contract.mobileAcademy.SmsStatusRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Set;


/**
 * Validator helper class for API request and response
 */
public final class MobileAcademyValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(MobileAcademyValidator.class);

    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    /**
     * Private constructor for static validation helpers
     */
    private MobileAcademyValidator() {

    }

    public static String validateCourseResponse(CourseResponse courseResponse) {

        LOGGER.debug("Validating course response request");
        Set<ConstraintViolation<CourseResponse>> violations = VALIDATOR.validate(courseResponse);
        StringBuilder sb = new StringBuilder();

        if (violations.size() > 0) {
            for (ConstraintViolation<CourseResponse> violation : violations) {
                sb.append(violation.getInvalidValue() + " : " +
                        violation.getPropertyPath() + " - " + violation.getMessage());
                sb.append("\n");
            }

            return sb.toString();
        } else {
            return null;
        }
    }

    public static String validateSmsStatus(SmsStatusRequest smsStatusRequest) {

        LOGGER.debug("validating sms status request");
        Set<ConstraintViolation<SmsStatusRequest>> violations = VALIDATOR.validate(smsStatusRequest);

        if (violations.size() == 0) {
            return null;
        }

        StringBuilder errors = new StringBuilder();
        for (ConstraintViolation<SmsStatusRequest> violation : violations) {

            errors.append(violation.getPropertyPath() + " : " + violation.getMessage());
            errors.append(", ");
        }

        return errors.toString().substring(0, errors.length() - 2);

    }
}
