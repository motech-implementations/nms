package org.motechproject.nms.region.domain.validation;

import org.motechproject.nms.region.domain.Village;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;


public class VillageValidator implements ConstraintValidator<ValidVillage, Village> {

    @Override
    public void initialize(ValidVillage validVillage) {

    }

    @Override
    public boolean isValid(Village village, ConstraintValidatorContext constraintValidatorContext) {
        if (village == null) {
            return true;
        }

        if (village.getVcode() == 0 && village.getSvid() == 0) {
            constraintValidatorContext
                    .buildConstraintViolationWithTemplate(constraintValidatorContext.getDefaultConstraintMessageTemplate())
                    .addNode("vcode").addConstraintViolation();
            return false;
        }

        return true;
    }
}
