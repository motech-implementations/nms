package org.motechproject.nms.region.language.domain.validation;

import org.motechproject.nms.region.language.domain.LanguageLocation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;


public class LanguageLocationValidator implements ConstraintValidator<ValidLanguageLocation, LanguageLocation> {

        @Override
        public void initialize(ValidLanguageLocation validLanguageLocation) {

        }

        @Override
        public boolean isValid(LanguageLocation languageLocation, ConstraintValidatorContext constraintValidatorContext) {
            if (languageLocation.getState() == null && languageLocation.getDistricts().isEmpty()) {
                return false;
            }

            return true;
        }
    }