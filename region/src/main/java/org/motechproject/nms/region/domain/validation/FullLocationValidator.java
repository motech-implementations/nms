package org.motechproject.nms.region.domain.validation;

import org.motechproject.nms.region.domain.District;
import org.motechproject.nms.region.domain.FullLocation;
import org.motechproject.nms.region.domain.HealthBlock;
import org.motechproject.nms.region.domain.HealthFacility;
import org.motechproject.nms.region.domain.HealthSubFacility;
import org.motechproject.nms.region.domain.Taluka;
import org.motechproject.nms.region.domain.Village;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class FullLocationValidator implements ConstraintValidator<ValidFullLocation, FullLocation> {

    @Override
    public void initialize(ValidFullLocation validFullLocation) {

    }

    @Override
    public boolean isValid(FullLocation location, ConstraintValidatorContext constraintValidatorContext) {
        boolean isValid = true;

        // A location hierarchy is valid if a value is set at the district level or lower and the chain
        // of locations is unbroken (with correct parent child relationships), or if no location is provided

        if (allNull(location)) {
            return true;
        }

        if (!validateHealthFacilities(location, constraintValidatorContext)) {
            isValid = false;
        }

        if (!validateLocationHierarchy(location, constraintValidatorContext)) {
            isValid = false;
        }

        return isValid;
    }

    private boolean allNull(FullLocation location) {
        return location.getState() == null && location.getDistrict() == null && location.getTaluka() == null &&
                location.getVillage() == null && location.getHealthBlock() == null &&
                location.getHealthFacility() == null && location.getHealthSubFacility() == null;
    }

    private boolean validateLocationHierarchy(FullLocation location, ConstraintValidatorContext constraintValidatorContext) { // NO CHECKSTYLE Cyclomatic Complexity
        boolean isValid = true;
        boolean locationAtOrBelowDistrict = false;

        if (location.getVillage() != null) {
            locationAtOrBelowDistrict = true;
            Village village = location.getVillage();

            if (location.getTaluka() == null) {
                constraintValidatorContext.disableDefaultConstraintViolation();
                constraintValidatorContext.buildConstraintViolationWithTemplate("Taluka must be set if village " +
                                                                         "is provided").addConstraintViolation();
                isValid = false;
            } else if (village.getTaluka() == null ||
                    !village.getTaluka().getId().equals(location.getTaluka().getId())) {
                constraintValidatorContext.disableDefaultConstraintViolation();
                constraintValidatorContext.buildConstraintViolationWithTemplate("Village is not a child of " +
                                                                          "the Taluka").addConstraintViolation();
                isValid = false;
            }
        }

        if (location.getTaluka() != null) {
            locationAtOrBelowDistrict = true;
            Taluka taluka = location.getTaluka();

            if (location.getDistrict() == null) {
                constraintValidatorContext.disableDefaultConstraintViolation();
                constraintValidatorContext.buildConstraintViolationWithTemplate("District must be set if " +
                                                                  "taluka is provided").addConstraintViolation();
                isValid = false;
            } else
                if (taluka.getDistrict() == null ||
                        !taluka.getDistrict().getId().equals(location.getDistrict().getId())) {
                    constraintValidatorContext.disableDefaultConstraintViolation();
                    constraintValidatorContext.buildConstraintViolationWithTemplate("Taluka is not a child of " +
                                                                        "the District").addConstraintViolation();
                    isValid = false;
                }
        }

        if (location.getDistrict() != null) {
            locationAtOrBelowDistrict = true;
            District district = location.getDistrict();

            if (location.getState() == null) {
                constraintValidatorContext.disableDefaultConstraintViolation();
                constraintValidatorContext.buildConstraintViolationWithTemplate("State must be set if district " +
                        "is provided").addConstraintViolation();
                isValid = false;
            } else
                if (district.getState() == null ||
                        !district.getState().getId().equals(location.getState().getId())) {
                    constraintValidatorContext.disableDefaultConstraintViolation();
                    constraintValidatorContext.buildConstraintViolationWithTemplate("District is not a child " +
                                                                        "of the State").addConstraintViolation();
                    isValid = false;
                }
        }

        if (isValid && !locationAtOrBelowDistrict) {
            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext.buildConstraintViolationWithTemplate("A location at District or below " +
                                                                    "must be provided").addConstraintViolation();
            isValid = false;
        }

        return isValid;
    }

    private boolean validateHealthFacilities(FullLocation location, ConstraintValidatorContext constraintValidatorContext) { // NO CHECKSTYLE Cyclomatic Complexity
        boolean isValid = true;

        if (location.getHealthSubFacility() != null) {
            HealthSubFacility healthSubFacility = location.getHealthSubFacility();

            if (location.getHealthFacility() == null) {
                constraintValidatorContext.disableDefaultConstraintViolation();
                constraintValidatorContext.buildConstraintViolationWithTemplate("Health Facility must be set if " +
                                                            "sub-facility is provided").addConstraintViolation();
                isValid = false;
            } else if (healthSubFacility.getHealthFacility() == null ||
                    !healthSubFacility.getHealthFacility().getId().equals(location.getHealthFacility().getId())) {
                constraintValidatorContext.disableDefaultConstraintViolation();
                constraintValidatorContext.buildConstraintViolationWithTemplate("Health Sub-Facility is not a " +
                                                        "child of the Health Facility").addConstraintViolation();
                isValid = false;
            }
        }

        if (location.getHealthFacility() != null) {
            HealthFacility healthFacility = location.getHealthFacility();

            if (location.getHealthBlock() == null) {
                constraintValidatorContext.disableDefaultConstraintViolation();
                constraintValidatorContext.buildConstraintViolationWithTemplate("Health Block must be set if " +
                                                               "facility is provided").addConstraintViolation();
                isValid = false;
            } else if (healthFacility.getHealthBlock() == null ||
                    !healthFacility.getHealthBlock().getId().equals(location.getHealthBlock().getId())) {
                constraintValidatorContext.disableDefaultConstraintViolation();
                constraintValidatorContext.buildConstraintViolationWithTemplate("Health Facility is not a " +
                                                           "child of the Health Block").addConstraintViolation();
                isValid = false;
            }
        }

        if (location.getHealthBlock() != null) {
            HealthBlock healthBlock = location.getHealthBlock();

            if (location.getTaluka() == null) {
                constraintValidatorContext.disableDefaultConstraintViolation();
                constraintValidatorContext.buildConstraintViolationWithTemplate("Taluka must be set if block " +
                                                                         "is provided").addConstraintViolation();
                isValid = false;
            } else if (healthBlock.getTaluka() == null ||
                    !healthBlock.getTaluka().getId().equals(location.getTaluka().getId())) {
                constraintValidatorContext.disableDefaultConstraintViolation();
                constraintValidatorContext.buildConstraintViolationWithTemplate("Health Block is not a child " +
                                                                      "of the Taluka").addConstraintViolation();
                isValid = false;
            }
        }

        return isValid;
    }
}
