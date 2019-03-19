package org.motechproject.nms.kilkari.service.impl;

import org.motechproject.nms.kilkari.domain.DeactivatedBeneficiary;
import org.motechproject.nms.kilkari.domain.DeactivationReason;
import org.motechproject.nms.kilkari.repository.DeactivatedBeneficiaryDataService;
import org.motechproject.nms.kilkari.service.DeactivatedBeneficiaryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.List;

@Service("deactivatedBeneficiaryService")
public class DeactivatedBeneficiaryServiceImpl implements DeactivatedBeneficiaryService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DeactivatedBeneficiaryServiceImpl.class);

    private DeactivatedBeneficiaryDataService deactivatedBeneficiaryDataService;

    @Autowired
    public DeactivatedBeneficiaryServiceImpl(DeactivatedBeneficiaryDataService deactivatedBeneficiaryDataService) {
        this.deactivatedBeneficiaryDataService = deactivatedBeneficiaryDataService;
    }

    public List<DeactivatedBeneficiary> findDeactivatedBeneficiariesOtherThanManualDeactivation(String externalId) {
        try {
            List<DeactivatedBeneficiary> deactivatedBeneficiaryWithSelectDeactivationReasons = new ArrayList<>();
            List<DeactivatedBeneficiary> deactivatedBeneficiaries = deactivatedBeneficiaryDataService.findByExternalId(externalId);
            if (deactivatedBeneficiaries != null && deactivatedBeneficiaries.size() != 0) {
                for (DeactivatedBeneficiary deactivatedBeneficiary : deactivatedBeneficiaries
                ) {
                    if (!DeactivationReason.LOW_LISTENERSHIP.equals(deactivatedBeneficiary.getDeactivationReason()) && !DeactivationReason.WEEKLY_CALLS_NOT_ANSWERED.equals(deactivatedBeneficiary.getDeactivationReason())) {
                        deactivatedBeneficiaryWithSelectDeactivationReasons.add(deactivatedBeneficiary);
                    }
                }
            }
            return deactivatedBeneficiaryWithSelectDeactivationReasons;
        } catch (ConstraintViolationException e) {
            LOGGER.error("1: List of constraints: {}", e.getConstraintViolations());
            throw e;
        }
    }
}
