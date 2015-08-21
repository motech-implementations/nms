package org.motechproject.nms.flw.domain.validation;

import org.motechproject.nms.flw.domain.FrontLineWorker;
import org.motechproject.nms.flw.domain.FrontLineWorkerStatus;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class FrontLineWorkerValidator implements ConstraintValidator<ValidFrontLineWorker, FrontLineWorker> {

    @Override
    public void initialize(ValidFrontLineWorker validFrontLineWorker) {

    }

    @Override
    public boolean isValid(FrontLineWorker flw, ConstraintValidatorContext constraintValidatorContext) {
        if (flw == null) {
            return true;
        }

        // An active FLW must have a state and district
        if (flw.getStatus() == FrontLineWorkerStatus.ACTIVE &&
                (flw.getState() == null || flw.getDistrict() == null)) {
            return false;
        }

        // Contact Number must be set unless the status is invalid
        if (flw.getContactNumber() == null && flw.getStatus() != FrontLineWorkerStatus.INVALID) {
            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext.buildConstraintViolationWithTemplate(
                    String.format("Contact Number can not be null for FLW with status %s", flw.getStatus()))
                            .addConstraintViolation();
            return false;
        }

        // invalid FLWs can't have a contact number
        if (flw.getStatus() == FrontLineWorkerStatus.INVALID && flw.getContactNumber() != null) {
            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext.buildConstraintViolationWithTemplate(
                    String.format("Invalid FLWs can not have a contact number set.", flw.getStatus()))
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}
