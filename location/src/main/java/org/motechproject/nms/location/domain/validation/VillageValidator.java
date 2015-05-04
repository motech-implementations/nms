package org.motechproject.nms.location.domain.validation;

import org.motechproject.nms.location.domain.Village;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;


public class VillageValidator implements ConstraintValidator<ValidVillage, Village> {

    @Override
    public void initialize(ValidVillage validVillage) {

    }

    @Override
    public boolean isValid(Village village, ConstraintValidatorContext constraintValidatorContext) {
        if (village.getVcode() == null && village.getSvid() == null) {
            return false;
        }

        return true;
    }
}