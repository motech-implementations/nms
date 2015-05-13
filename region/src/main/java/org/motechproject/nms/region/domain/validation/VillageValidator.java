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

        if (village.getVcode() == null && village.getSvid() == null) {
            constraintValidatorContext
                    .buildConstraintViolationWithTemplate(constraintValidatorContext.getDefaultConstraintMessageTemplate())
                    .addPropertyNode("vcode").addConstraintViolation();
            return false;
        }

        return true;
    }
}
