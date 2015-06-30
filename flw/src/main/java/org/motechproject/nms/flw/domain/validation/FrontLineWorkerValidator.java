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

        if (flw.getStatus() == FrontLineWorkerStatus.ACTIVE &&
                (flw.getState() == null || flw.getDistrict() == null)) {
            return false;
        }

        return true;
    }
}
